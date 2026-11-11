// Validation robuste : re-teste chaque candidat cassé plusieurs fois avant de
// le déclarer définitivement mort. Élimine les faux positifs (flakiness réseau).
// Lit scripts/broken_images.json (candidats), écrit la liste CONFIRMÉE au même endroit.
import fs from 'node:fs';

const CACHE_FILE = 'scripts/image_cache.json';
const BROKEN = 'scripts/broken_images.json';
const RETRIES = 3;          // nb de tentatives supplémentaires avant de condamner
const TIMEOUT_MS = 15000;
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36';
const sleep = (ms) => new Promise(r => setTimeout(r, ms));

const cache = JSON.parse(fs.readFileSync(CACHE_FILE, 'utf8'));
const candidates = JSON.parse(fs.readFileSync(BROKEN, 'utf8'));

async function loadsOnce(url) {
  if (!url) return false;
  const ctrl = new AbortController();
  const t = setTimeout(() => ctrl.abort(), TIMEOUT_MS);
  try {
    const res = await fetch(url, { method: 'GET', headers: { 'User-Agent': UA, Accept: 'image/*,*/*' }, redirect: 'follow', signal: ctrl.signal });
    clearTimeout(t);
    if (!res.ok) return false;
    return (res.headers.get('content-type') || '').startsWith('image/');
  } catch { clearTimeout(t); return false; }
}

// vrai = charge au moins une fois sur RETRIES+1 essais
async function loadsEventually(url) {
  for (let i = 0; i <= RETRIES; i++) {
    if (await loadsOnce(url)) return true;
    await sleep(800);
  }
  return false;
}

const confirmed = [];
let done = 0;
for (const key of candidates) {
  const ok = await loadsEventually(cache[key]);
  done++;
  if (!ok) confirmed.push(key);
  console.log(`[${done}/${candidates.length}] ${key.split('|')[0]} -> ${ok ? 'OK (faux positif)' : 'CASSÉE'}`);
}

fs.writeFileSync(BROKEN, JSON.stringify(confirmed, null, 2));
console.log(`\n✅ ${confirmed.length} réellement cassées sur ${candidates.length} candidats (${BROKEN})`);
