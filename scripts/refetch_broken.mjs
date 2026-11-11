// Re-scrape les images cassées (scripts/broken_images.json) en VALIDANT chaque
// candidat (200 + content-type image) avant de l'accepter. Met à jour le cache.
import fs from 'node:fs';

const CACHE_FILE = 'scripts/image_cache.json';
const BROKEN = 'scripts/broken_images.json';
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36';
const MAX_CANDIDATES = 8;
const sleep = (ms) => new Promise(r => setTimeout(r, ms));

function formeKeyword(forme) {
  const f = (forme || '').toLowerCase();
  const has = (s) => f.includes(s);
  if (has('collyre') || has('ophtalmique')) return 'collyre';
  if (has('sirop')) return 'sirop';
  if (has('suppositoire')) return 'suppositoire';
  if (has('ovule') || has('gynecologique')) return 'ovule';
  if (has('perfusion') || has('injectable')) return 'ampoule injectable';
  if (has('creme')) return 'creme';
  if (has('pommade')) return 'pommade';
  if (has('lait dermique') || has('lotion')) return 'lotion';
  if (has('shampooing')) return 'shampoing';
  if (has('gel')) return 'gel';
  if (has('nasal')) return 'spray nasal';
  if (has('auriculaire')) return 'gouttes auriculaires';
  if (has('inhalation') || has('aerosol')) return 'inhalateur';
  if (has('trans-dermique') || has('transdermique')) return 'patch';
  if (has('vernis')) return 'vernis';
  if (has('gouttes') || has('buvable')) return 'sirop';
  if (has('suspension')) return 'suspension buvable';
  if (has('effervescent')) return 'comprime effervescent';
  if (has('gelule')) return 'gelule';
  if (has('capsule')) return 'capsule';
  if (has('comprime')) return 'comprime';
  if (has('poudre')) return 'poudre';
  return '';
}

async function getVqd(query) {
  const res = await fetch(`https://duckduckgo.com/?q=${encodeURIComponent(query)}&iax=images&ia=images`, { headers: { 'User-Agent': UA } });
  const html = await res.text();
  const m = html.match(/vqd=["']([\d-]+)["']/);
  return m ? m[1] : null;
}

async function searchResults(query) {
  const vqd = await getVqd(query);
  if (!vqd) return [];
  await sleep(500);
  const url = `https://duckduckgo.com/i.js?l=us-en&o=json&q=${encodeURIComponent(query)}&vqd=${vqd}&f=,,,&p=1`;
  const res = await fetch(url, { headers: { 'User-Agent': UA, Referer: 'https://duckduckgo.com/', Accept: 'application/json' } });
  if (res.status !== 200) return null; // null = à retenter (rate-limit)
  const data = await res.json();
  return (data.results || []).map(r => r.image).filter(Boolean);
}

async function isValidImage(url) {
  const ctrl = new AbortController();
  const t = setTimeout(() => ctrl.abort(), 12000);
  try {
    const res = await fetch(url, { headers: { 'User-Agent': UA, Accept: 'image/*,*/*' }, redirect: 'follow', signal: ctrl.signal });
    clearTimeout(t);
    if (!res.ok) return false;
    return (res.headers.get('content-type') || '').startsWith('image/');
  } catch { clearTimeout(t); return false; }
}

async function findValidImage(query) {
  for (let attempt = 0; attempt < 4; attempt++) {
    const results = await searchResults(query);
    if (results === null) { await sleep(3000 * (attempt + 1)); continue; } // rate-limit
    for (const candidate of results.slice(0, MAX_CANDIDATES)) {
      if (await isValidImage(candidate)) return candidate;
    }
    return ''; // résultats obtenus mais aucun valide
  }
  return '';
}

const cache = JSON.parse(fs.readFileSync(CACHE_FILE, 'utf8'));
const broken = JSON.parse(fs.readFileSync(BROKEN, 'utf8'));

let fixed = 0, stillBad = 0, done = 0;
for (const key of broken) {
  const [name, dosage, forme] = key.split('|');
  const query = `${name} ${dosage} ${formeKeyword(forme)}`.replace(/\s+/g, ' ').trim();
  const url = await findValidImage(query);
  done++;
  if (url) { cache[key] = url; fixed++; } else { stillBad++; }
  fs.writeFileSync(CACHE_FILE, JSON.stringify(cache, null, 2));
  console.log(`[${done}/${broken.length}] ${name} ${dosage} -> ${url ? 'CORRIGÉ' : 'introuvable'}`);
  await sleep(1000);
}

console.log(`\n✅ Terminé : ${fixed} corrigées, ${stillBad} sans image valide.`);
