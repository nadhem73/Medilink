// Rewrites the 3 MediLink n8n workflows as: Webhook -> single Code node.
// Reason: in n8n 2.8.4 here, {{ }} expressions in HTTP Request nodes are not
// evaluated (empty bodies). Code nodes call APIs via this.helpers.httpRequest().
//
// n8n MUST be stopped before running this (it caches active workflows).
// Usage: node scripts/n8n-fix-workflows.js
const fs = require('fs');
const path = require('path');
const os = require('os');
const Database = require('better-sqlite3');

const DB_PATH = path.join(os.homedir(), '.n8n', 'database.sqlite');
const TOKEN = '8644047281:AAHywXMMM9zpDThyBj_FOgme46RZi_GhzRk';
const AUTH_URL = 'http://localhost:8084';          // GET /api/pharmacies (public)
const PRESCRIPTION_URL = 'http://localhost:8086';  // PUT /api/prescriptions/{id}/assign-pharmacy (public)

const header = `const TOKEN = '${TOKEN}';
const TG = 'https://api.telegram.org/bot' + TOKEN;
const AUTH_URL = '${AUTH_URL}';
const PRESCRIPTION_URL = '${PRESCRIPTION_URL}';
const http = (opts) => this.helpers.httpRequest({ json: true, ...opts });
`;

const WF1_CODE = header + `
const body = $input.first().json.body || {};
if (!body.patientTelegramChatId) {
  return [{ json: { skipped: true, reason: 'No patientTelegramChatId', prescriptionId: body.id } }];
}
const chatId = body.patientTelegramChatId;
const prescriptionId = body.id;

const pharmacies = await http({ method: 'GET', url: AUTH_URL + '/api/pharmacies' });
const list = Array.isArray(pharmacies) ? pharmacies : (pharmacies.data || []);

const keyboard = [];
for (let i = 0; i < list.length; i += 2) {
  keyboard.push(list.slice(i, i + 2).map(p => ({
    text: p.pharmacyName || ('Pharmacie #' + p.id),
    callback_data: 'pick:' + prescriptionId + ':' + p.id,
  })));
}

const text = '\\u{1F3E5} *Nouvelle ordonnance médicale* – N° ' + prescriptionId +
  '\\n\\nBonjour,\\n\\nUne ordonnance électronique vient d\\u2019être établie à votre nom par votre médecin traitant.' +
  '\\n\\nVeuillez sélectionner la pharmacie de votre choix ci-dessous pour sa préparation :';

const res = await http({
  method: 'POST', url: TG + '/sendMessage',
  body: { chat_id: chatId, text, parse_mode: 'Markdown', reply_markup: { inline_keyboard: keyboard } },
});
return [{ json: { ok: res.ok, messageId: res.result && res.result.message_id, prescriptionId, pharmacies: list.length } }];
`;

const WF2_CODE = header + `
const body = $input.first().json.body || {};
const cq = body.callback_query;
if (!cq) {
  return [{ json: { skipped: true, reason: 'Not a callback_query update' } }];
}
const chatId = cq.from && cq.from.id;
const parts = String(cq.data || '').split(':');
if (parts[0] !== 'pick' || parts.length < 3) {
  await http({ method: 'POST', url: TG + '/answerCallbackQuery', body: { callback_query_id: cq.id, text: 'Action inconnue.' } });
  return [{ json: { error: 'Invalid callback format: ' + cq.data } }];
}
const prescriptionId = parseInt(parts[1], 10);
const pharmacyId = parseInt(parts[2], 10);

try {
  await http({
    method: 'PUT', url: PRESCRIPTION_URL + '/api/prescriptions/' + prescriptionId + '/assign-pharmacy',
    body: { pharmacyId },
  });
} catch (e) {
  await http({ method: 'POST', url: TG + '/answerCallbackQuery',
    body: { callback_query_id: cq.id, text: '❌ Impossible d\\u2019assigner la pharmacie. Réessayez plus tard.', show_alert: true } });
  return [{ json: { error: 'assign-pharmacy failed: ' + e.message, prescriptionId, pharmacyId } }];
}

let pharmacyName = 'Pharmacie #' + pharmacyId;
try {
  const list = await http({ method: 'GET', url: AUTH_URL + '/api/pharmacies' });
  const p = (Array.isArray(list) ? list : []).find(x => x.id === pharmacyId);
  if (p && p.pharmacyName) pharmacyName = p.pharmacyName;
} catch (e) { /* keep fallback name */ }

await http({ method: 'POST', url: TG + '/answerCallbackQuery',
  body: { callback_query_id: cq.id, text: '✅ Pharmacie confirmée avec succès.' } });

// Remove the buttons so the patient cannot pick twice
if (cq.message) {
  try {
    await http({ method: 'POST', url: TG + '/editMessageReplyMarkup',
      body: { chat_id: cq.message.chat.id, message_id: cq.message.message_id, reply_markup: { inline_keyboard: [] } } });
  } catch (e) { /* not critical */ }
}

const text = '✅ *Pharmacie confirmée*\\n\\n\\u{1F3E5} *' + pharmacyName + '* a bien été sélectionnée pour votre ordonnance N°' +
  prescriptionId + '.\\n\\n⏳ Vous recevrez une notification dès que vos médicaments seront prêts.' +
  '\\n\\n—\\n_MédiLink Tunisie – Votre santé, notre priorité._';
const res = await http({ method: 'POST', url: TG + '/sendMessage', body: { chat_id: chatId, text, parse_mode: 'Markdown' } });
return [{ json: { ok: res.ok, prescriptionId, pharmacyId, pharmacyName } }];
`;

const WF3_CODE = header + `
const body = $input.first().json.body || {};
const status = body.status || (body.pickupCode ? 'PREPAREE' : null);
if (!body.patientTelegramChatId) {
  return [{ json: { skipped: true, reason: 'No patientTelegramChatId', prescriptionId: body.id, status } }];
}
const FOOTER = '\\n\\n—\\n_MédiLink Tunisie – Votre santé, notre priorité._';

let pharmacyName = null;
if (body.pharmacyId) {
  try {
    const list = await http({ method: 'GET', url: AUTH_URL + '/api/pharmacies' });
    const p = (Array.isArray(list) ? list : []).find(x => x.id === Number(body.pharmacyId));
    if (p && p.pharmacyName) pharmacyName = p.pharmacyName;
  } catch (e) { /* name is optional */ }
}
const chez = pharmacyName ? ('*' + pharmacyName + '*') : 'Votre pharmacie';

let text;
switch (status) {
  case 'PREPAREE':
    if (!body.pickupCode) {
      return [{ json: { skipped: true, reason: 'PREPAREE without pickupCode', prescriptionId: body.id } }];
    }
    text = '✅ *Ordonnance prête au retrait* – N° ' + body.id +
      '\\n\\n\\u{1F4E6} Vos médicaments sont prêts et vous attendent ' + (pharmacyName ? 'à ' + chez : 'en pharmacie') + '.' +
      '\\n\\n\\u{1F511} *Code de retrait :* \`' + body.pickupCode + '\`' +
      '\\n\\nPrésentez ce code à votre pharmacien pour récupérer vos médicaments.' +
      '\\n\\n⏱️ Nous vous recommandons de venir retirer votre commande dans les plus brefs délais.' + FOOTER;
    break;
  case 'EN_PREPARATION':
    text = '\\u{1F48A} *Ordonnance en préparation* – N° ' + body.id +
      '\\n\\n' + chez + ' a pris en charge votre ordonnance et prépare vos médicaments.' +
      '\\n\\n⏳ Vous recevrez votre code de retrait dès qu\\u2019elle sera prête.' + FOOTER;
    break;
  case 'RETIREE':
    text = '\\u{1F6CD}️ *Ordonnance retirée* – N° ' + body.id +
      '\\n\\nVos médicaments ont bien été remis. Merci de votre confiance !' +
      '\\n\\n\\u{1F49A} Nous vous souhaitons un prompt rétablissement. Respectez bien la posologie indiquée par votre médecin.' + FOOTER;
    break;
  case 'ANNULEE':
    text = '❌ *Ordonnance annulée* – N° ' + body.id +
      '\\n\\nVotre médecin a annulé cette ordonnance. Elle ne peut plus être utilisée en pharmacie.' +
      '\\n\\nPour toute question, contactez votre médecin traitant.' + FOOTER;
    break;
  default:
    return [{ json: { skipped: true, reason: 'No message for status ' + status, prescriptionId: body.id } }];
}
const res = await http({ method: 'POST', url: TG + '/sendMessage',
  body: { chat_id: body.patientTelegramChatId, text, parse_mode: 'Markdown' } });
return [{ json: { ok: res.ok, messageId: res.result && res.result.message_id, prescriptionId: body.id, status } }];
`;

function build(webhookName, webhookPath, codeName, code) {
  const nodes = [
    {
      parameters: { httpMethod: 'POST', path: webhookPath, responseMode: 'onReceived', options: {} },
      name: webhookName, type: 'n8n-nodes-base.webhook', typeVersion: 1, position: [250, 300],
      webhookId: undefined,
    },
    {
      parameters: { jsCode: code },
      name: codeName, type: 'n8n-nodes-base.code', typeVersion: 2, position: [500, 300],
    },
  ];
  const connections = { [webhookName]: { main: [[{ node: codeName, type: 'main', index: 0 }]] } };
  return { nodes, connections };
}

const WORKFLOWS = {
  iYlm5Sv4hR97rixn: build('Webhook', 'prescription-created', 'Envoyer ordonnance + choix pharmacie', WF1_CODE),
  tZwHDkw1PqSAYw1F: build('Webhook', 'telegram-callback', 'Traiter choix pharmacie', WF2_CODE),
  ScrkfsBPpbxh1227: build('Webhook', 'prescription-prepared', 'Notifier patient (statut)', WF3_CODE),
};

const backup = DB_PATH + '.bak-' + Date.now();
fs.copyFileSync(DB_PATH, backup);
console.log('Backup:', backup);

const db = new Database(DB_PATH);
const tx = db.transaction(() => {
  for (const [id, wf] of Object.entries(WORKFLOWS)) {
    const row = db.prepare('SELECT nodes, activeVersionId, versionId FROM workflow_entity WHERE id = ?').get(id);
    if (!row) throw new Error('Workflow not found: ' + id);
    // keep original node ids / webhookIds so webhook registrations stay stable
    const old = JSON.parse(row.nodes);
    for (const n of wf.nodes) {
      const prev = old.find(o => o.type === n.type && (n.type !== 'n8n-nodes-base.webhook' || o.name === n.name));
      n.id = (prev && prev.id) || require('crypto').randomUUID();
      if (prev && prev.webhookId) n.webhookId = prev.webhookId; else delete n.webhookId;
    }
    const nodes = JSON.stringify(wf.nodes);
    const conns = JSON.stringify(wf.connections);
    db.prepare('UPDATE workflow_entity SET nodes = ?, connections = ? WHERE id = ?').run(nodes, conns, id);
    const versions = [...new Set([row.activeVersionId, row.versionId].filter(Boolean))];
    for (const v of versions) {
      db.prepare('UPDATE workflow_history SET nodes = ?, connections = ? WHERE versionId = ?').run(nodes, conns, v);
    }
    console.log('Updated', id, 'versions', versions.join(','));
  }
});
tx();
db.close();
console.log('Done. Restart n8n.');
