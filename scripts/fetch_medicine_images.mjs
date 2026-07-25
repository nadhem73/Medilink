// Enrichit medicine_data_tn.csv avec une colonne "Image"
// Source: DuckDuckGo Image Search (gratuit, sans clé API)
// Usage:
//   node scripts/fetch_medicine_images.mjs --test        -> teste sur 3 médicaments
//   node scripts/fetch_medicine_images.mjs               -> traite tout le dataset

import fs from 'node:fs';
import path from 'node:path';

const CSV_IN = 'Datasets/medicine_data_tn.csv';
const CSV_OUT = 'Datasets/medicine_data_tn_with_images.csv';
const CACHE_FILE = 'scripts/image_cache.json';
const DELIM = ';';
const isTest = process.argv.includes('--test');

const UA =
  'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36';

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

// --- Mappe la Forme du dataset vers un mot-clé court qui améliore la recherche ---
// Le dataset encode les accents en "�" -> on matche sur des sous-chaînes sans accent.
function formeKeyword(forme) {
  const f = (forme || '').toLowerCase();
  const has = (s) => f.includes(s);
  if (has('collyre')) return 'collyre';
  if (has('ophtalmique')) return 'collyre';
  if (has('sirop')) return 'sirop';
  if (has('suppositoire')) return 'suppositoire';
  if (has('ovule')) return 'ovule';
  if (has('gynecologique')) return 'ovule gynecologique';
  if (has('perfusion') || has('injectable')) return 'ampoule injectable';
  if (has('creme')) return 'creme';
  if (has('pommade')) return 'pommade';
  if (has('lait dermique') || has('lotion')) return 'lotion';
  if (has('shampooing')) return 'shampoing';
  if (has('gel')) return 'gel';
  if (has('spray nasal') || has('nasal')) return 'spray nasal';
  if (has('auriculaire')) return 'gouttes auriculaires';
  if (has('inhalation') || has('aerosol')) return 'inhalateur';
  if (has('trans-dermique') || has('transdermique')) return 'patch';
  if (has('vernis')) return 'vernis';
  if (has('gouttes buvables') || has('solution buvable') || has('suspension buvable') || has('gouttes'))
    return 'sirop gouttes';
  if (has('suspension orale') || has('solution orale') || has('suspension')) return 'suspension buvable';
  if (has('effervescent')) return 'comprime effervescent';
  if (has('gelule')) return 'gelule';
  if (has('capsule')) return 'capsule';
  if (has('comprime')) return 'comprime';
  if (has('poudre')) return 'poudre';
  return '';
}

// --- CSV parsing (simple, le dataset n'a pas de champs entre guillemets) ---
function parseCSV(text) {
  const lines = text.replace(/\r/g, '').split('\n').filter((l) => l.length > 0);
  const header = lines[0].split(DELIM);
  const rows = lines.slice(1).map((l) => l.split(DELIM));
  return { header, rows };
}

// --- Récupère le token vqd requis par DuckDuckGo ---
async function getVqd(query) {
  const res = await fetch(
    `https://duckduckgo.com/?q=${encodeURIComponent(query)}&iax=images&ia=images`,
    { headers: { 'User-Agent': UA } }
  );
  const html = await res.text();
  const m =
    html.match(/vqd=["']([\d-]+)["']/) || html.match(/vqd=([\d-]+)&/);
  return m ? m[1] : null;
}

// --- Une tentative unique. Renvoie: url | '__EMPTY__' (rate-limit probable) | null (erreur) ---
async function searchOnce(query) {
  try {
    const vqd = await getVqd(query);
    if (!vqd) return null;
    await sleep(600);
    const url = `https://duckduckgo.com/i.js?l=us-en&o=json&q=${encodeURIComponent(
      query
    )}&vqd=${vqd}&f=,,,&p=1`;
    const res = await fetch(url, {
      headers: {
        'User-Agent': UA,
        Referer: 'https://duckduckgo.com/',
        Accept: 'application/json, text/javascript, */*; q=0.01',
      },
    });
    if (res.status !== 200) return null;
    const data = await res.json();
    if (data.results && data.results.length) return data.results[0].image || '';
    return '__EMPTY__'; // DDG renvoie souvent [] quand il nous limite -> à retenter
  } catch (e) {
    return null;
  }
}

// --- Recherche avec retries + backoff. Retente aussi sur résultats vides. ---
async function searchImage(query) {
  for (let i = 0; i < 5; i++) {
    const res = await searchOnce(query);
    if (res && res !== '__EMPTY__') return res;
    await sleep(3000 * (i + 1)); // backoff progressif: 3s, 6s, 9s, 12s
  }
  return ''; // vraiment introuvable après 5 essais
}

async function main() {
  const text = fs.readFileSync(CSV_IN, 'utf8');
  const { header, rows } = parseCSV(text);
  const nameIdx = header.indexOf('Name');
  const dosageIdx = header.indexOf('Dosage');
  const formeIdx = header.indexOf('Forme');

  const cache = fs.existsSync(CACHE_FILE)
    ? JSON.parse(fs.readFileSync(CACHE_FILE, 'utf8'))
    : {};

  const outHeader = [...header, 'Image'];
  const targetRows = isTest ? rows.slice(0, 3) : rows;

  const outRows = [];
  let done = 0;
  for (const row of targetRows) {
    const name = row[nameIdx] || '';
    const dosage = row[dosageIdx] || '';
    const forme = row[formeIdx] || '';
    // clé de cache par médicament + dosage + forme
    const key = `${name}|${dosage}|${forme}`;
    let img = cache[key];
    if (!img) {
      // requête = nom + dosage + mot-clé de forme (distingue sirop/comprimé/crème/collyre…)
      const query = `${name} ${dosage} ${formeKeyword(forme)}`.replace(/\s+/g, ' ').trim();
      img = await searchImage(query);
      if (img) {
        // on ne cache que les succès -> les échecs seront retentés au prochain run
        cache[key] = img;
        fs.writeFileSync(CACHE_FILE, JSON.stringify(cache, null, 2));
      }
      await sleep(1200); // politesse anti rate-limit
    }
    outRows.push([...row, img]);
    done++;
    console.log(`[${done}/${targetRows.length}] ${name} ${dosage} -> ${img ? 'OK' : 'vide'}`);
  }

  if (isTest) {
    console.log('\n--- TEST résultats ---');
    outRows.forEach((r) => console.log(r[nameIdx], '=>', r[r.length - 1]));
    return;
  }

  const outText =
    outHeader.join(DELIM) + '\n' + outRows.map((r) => r.join(DELIM)).join('\n') + '\n';
  fs.writeFileSync(CSV_OUT, outText, 'utf8');
  console.log(`\n✅ Écrit: ${CSV_OUT} (${outRows.length} lignes)`);
}

main();
