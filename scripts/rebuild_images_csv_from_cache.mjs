// Reconstruit Datasets/medicine_data_tn_with_images.csv à partir du cache (sans re-scraper).
// Les clés listées dans broken_images.json (encore cassées) sont vidées -> placeholder propre.
import fs from 'node:fs';

const SRC = 'Datasets/medicine_data_tn.csv';
const OUT = 'Datasets/medicine_data_tn_with_images.csv';
const CACHE_FILE = 'scripts/image_cache.json';
const BROKEN = 'scripts/broken_images.json';
const DELIM = ';';

const cache = JSON.parse(fs.readFileSync(CACHE_FILE, 'utf8'));
const broken = new Set(fs.existsSync(BROKEN) ? JSON.parse(fs.readFileSync(BROKEN, 'utf8')) : []);

const lines = fs.readFileSync(SRC, 'utf8').replace(/\r/g, '').split('\n').filter(l => l.length);
const header = lines[0].split(DELIM);
const nameIdx = header.indexOf('Name');
const dosageIdx = header.indexOf('Dosage');
const formeIdx = header.indexOf('Forme');

const out = [header.join(DELIM) + DELIM + 'Image'];
let withImg = 0, blanked = 0;

for (let i = 1; i < lines.length; i++) {
  const cols = lines[i].split(DELIM);
  const key = `${cols[nameIdx]}|${cols[dosageIdx]}|${cols[formeIdx]}`;
  let img = cache[key] || '';
  if (broken.has(key)) { img = ''; blanked++; }   // encore cassée -> placeholder
  if (img) withImg++;
  out.push(lines[i] + DELIM + img);
}

fs.writeFileSync(OUT, out.join('\n') + '\n', 'utf8');
console.log(`✅ ${OUT} reconstruit : ${withImg} avec image, ${blanked} vidées (cassées).`);
