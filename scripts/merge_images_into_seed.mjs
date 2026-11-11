// Fusionne la colonne Image (Datasets/medicine_data_tn_with_images.csv, séparé par ';')
// dans le CSV du seeder backend (data/medicaments.csv, séparé par ',') en ajoutant
// une colonne "image_url". Les deux fichiers ont les mêmes lignes dans le même ordre.

import fs from 'node:fs';

const SEED = 'Backend/pharmacy-service/src/main/resources/data/medicaments.csv';
const IMAGES = 'Datasets/medicine_data_tn_with_images.csv';

const readLines = (p) => fs.readFileSync(p, 'utf8').replace(/\r/g, '').split('\n');

// Échappe une valeur pour un CSV séparé par virgules (RFC 4180)
function csvEscape(v) {
  if (v == null) v = '';
  if (v.includes(',') || v.includes('"') || v.includes('\n')) {
    return '"' + v.replace(/"/g, '""') + '"';
  }
  return v;
}

const seedLines = readLines(SEED);
const imgLines = readLines(IMAGES);

if (seedLines.length !== imgLines.length) {
  console.error(`❌ Nombre de lignes différent: seed=${seedLines.length} images=${imgLines.length}`);
  process.exit(1);
}

const out = [];
let withImg = 0;
let mismatch = 0;

for (let i = 0; i < seedLines.length; i++) {
  const seed = seedLines[i];
  if (seed === '') { out.push(''); continue; }

  if (i === 0) {
    out.push(seed + ',image_url'); // entête
    continue;
  }

  // colonne Image = dernier champ du CSV images (les URLs ne contiennent pas ';')
  const imgCols = imgLines[i].split(';');
  const image = imgCols[imgCols.length - 1] || '';

  // garde-fou: vérifier que le nom (1re colonne) concorde
  const seedName = seed.split(',')[0];
  const imgName = imgCols[0];
  if (seedName !== imgName) {
    mismatch++;
    if (mismatch <= 5) console.warn(`  ⚠ ligne ${i}: seed="${seedName}" vs image="${imgName}"`);
  }

  if (image) withImg++;
  out.push(seed + ',' + csvEscape(image));
}

fs.writeFileSync(SEED, out.join('\n'), 'utf8');
console.log(`✅ ${SEED} mis à jour`);
console.log(`   lignes: ${seedLines.length - 1} | avec image: ${withImg} | désalignements: ${mismatch}`);
