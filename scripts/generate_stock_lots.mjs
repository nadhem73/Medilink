// Régénère Backend/.../data/stock_medicaments.csv en gestion PAR LOTS.
// Pour chaque médicament : 2 à 3 lots, chacun avec un numéro de lot distinct
// (format AAMM-Xnnn = année/mois de fabrication + suffixe), une date d'expiration
// DISTINCTE, et la règle : lot ancien (fabriqué avant) expire AVANT le lot récent.
//
// Réutilise la liste des medicament_id présents dans l'ancien CSV.
import fs from 'node:fs';

const CSV = 'Backend/pharmacy-service/src/main/resources/data/stock_medicaments.csv';
const TODAY = new Date('2026-07-09');

// --- helpers dates ---------------------------------------------------------
const pad = (n) => String(n).padStart(2, '0');
const fmt = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
const addMonths = (d, m) => { const x = new Date(d); x.setMonth(x.getMonth() + m); return x; };
const addDays = (d, n) => { const x = new Date(d); x.setDate(x.getDate() + n); return x; };
const rand = (a, b) => a + Math.floor(Math.random() * (b - a + 1));
const pick = (arr) => arr[Math.floor(Math.random() * arr.length)];

// --- numéro de lot : AAMM-Xnnn (unicité garantie via Set) ------------------
const LETTERS = 'ABCDEFGHJKLMNPRTVWXYZ';
const usedLots = new Set();
function makeLot(fab) {
  const base = `${pad(fab.getFullYear() % 100)}${pad(fab.getMonth() + 1)}`;
  let code;
  do {
    code = `${base}-${pick(LETTERS)}${pad(rand(0, 999)).padStart(3, '0')}`;
  } while (usedLots.has(code));
  usedLots.add(code);
  return code;
}

// --- emplacement type rayon pharmacie --------------------------------------
const ZONES = ['A', 'B', 'C', 'D', 'Rayon'];
const makeEmplacement = () => {
  const z = pick(ZONES);
  return z === 'Rayon' ? `Rayon${rand(1, 9)}` : `${z}${rand(1, 20)}`;
};

// --- lecture des medicament_id existants ------------------------------------
const lines = fs.readFileSync(CSV, 'utf8').replace(/\r/g, '').split('\n').filter(l => l.trim());
lines.shift(); // entête
const ids = [...new Set(lines.map(l => l.split(',')[0].trim()).filter(Boolean))]
  .map(Number).sort((a, b) => a - b);

// --- génération -------------------------------------------------------------
const out = ['medicament_id,numero_lot,quantite_en_stock,date_fabrication,date_expiration,emplacement,dernier_reapprovisionnement'];
let totalLots = 0;

for (const id of ids) {
  const nbLots = rand(2, 3);

  // Dates de fabrication croissantes : le 1er lot est le plus ANCIEN.
  // On part de 6 à 30 mois dans le passé, puis on avance de 4 à 12 mois par lot.
  let fab = addMonths(TODAY, -rand(18, 34));
  let prevExp = null;

  for (let i = 0; i < nbLots; i++) {
    // durée de conservation 24–42 mois → expiration
    let exp = addMonths(fab, rand(24, 42));
    // garantir expiration STRICTEMENT croissante (ancien expire avant récent)
    if (prevExp && exp <= prevExp) exp = addMonths(prevExp, rand(2, 6));
    prevExp = exp;

    // dernier réappro : entre la fabrication et aujourd'hui
    const spanDays = Math.max(1, Math.floor((TODAY - fab) / 86400000));
    const reappro = addDays(fab, rand(0, spanDays));

    const lot = makeLot(fab);
    const qte = rand(4, 200);
    const emp = makeEmplacement();

    out.push([id, lot, qte, fmt(fab), fmt(exp), emp, fmt(reappro)].join(','));
    totalLots++;

    // lot suivant fabriqué plus récemment
    fab = addMonths(fab, rand(4, 12));
    if (fab > TODAY) fab = addMonths(TODAY, -rand(0, 3));
  }
}

fs.writeFileSync(CSV, out.join('\n') + '\n', 'utf8');
console.log(`✅ ${CSV} régénéré : ${ids.length} médicaments, ${totalLots} lots.`);
console.log(`   Répartition ≈ ${(totalLots / ids.length).toFixed(2)} lots/médicament.`);
