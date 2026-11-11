// Valide chaque URL d'image du cache : charge-t-elle vraiment (200 + type image) ?
// Écrit la liste des cassées dans scripts/broken_images.json
import fs from 'node:fs';

const CACHE_FILE = 'scripts/image_cache.json';
const OUT = 'scripts/broken_images.json';
const CONCURRENCY = 24;
const TIMEOUT_MS = 12000;
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36';

const cache = JSON.parse(fs.readFileSync(CACHE_FILE, 'utf8'));
const entries = Object.entries(cache); // [key, url]

async function checkUrl(url) {
  if (!url) return false;
  const ctrl = new AbortController();
  const t = setTimeout(() => ctrl.abort(), TIMEOUT_MS);
  try {
    const res = await fetch(url, {
      method: 'GET',
      headers: { 'User-Agent': UA, 'Accept': 'image/*,*/*' },
      redirect: 'follow',
      signal: ctrl.signal,
    });
    clearTimeout(t);
    if (!res.ok) return false;
    const ct = res.headers.get('content-type') || '';
    return ct.startsWith('image/');
  } catch {
    clearTimeout(t);
    return false;
  }
}

const broken = [];
let done = 0;

async function worker(queue) {
  while (queue.length) {
    const [key, url] = queue.pop();
    const ok = await checkUrl(url);
    done++;
    if (!ok) broken.push(key);
    if (done % 100 === 0) console.log(`${done}/${entries.length} testées — ${broken.length} cassées`);
  }
}

const queue = [...entries];
await Promise.all(Array.from({ length: CONCURRENCY }, () => worker(queue)));

fs.writeFileSync(OUT, JSON.stringify(broken, null, 2));
console.log(`\n✅ Terminé : ${broken.length} images cassées sur ${entries.length} (${OUT})`);
