const http = require('http');
const https = require('https');

const TOKEN = '8644047281:AAHywXMMM9zpDThyBj_FOgme46RZi_GhzRk';

function apiRequest(options, body) {
  return new Promise((resolve, reject) => {
    const client = options.hostname === 'api.telegram.org' ? https : http;
    const req = client.request(options, res => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        const isError = res.statusCode < 200 || res.statusCode >= 300;
        try {
          const parsed = JSON.parse(data);
          if (isError) reject(new Error(parsed.error || parsed.message || data));
          else resolve(parsed);
        } catch(e) {
          if (isError) reject(new Error(data));
          else resolve(data);
        }
      });
    });
    req.on('error', reject);
    if (body) req.write(JSON.stringify(body));
    req.end();
  });
}

function getPharmacies() {
  return apiRequest({ hostname: 'localhost', port: 8081, path: '/api/pharmacies', method: 'GET', headers: { 'Content-Type': 'application/json' } });
}

function assignPharmacy(prescriptionId, pharmacyId) {
  return apiRequest({
    hostname: 'localhost', port: 8765, path: `/api/prescriptions/${prescriptionId}/assign-pharmacy`,
    method: 'PUT', headers: { 'Content-Type': 'application/json' }
  }, { pharmacyId });
}

function answerCallback(callbackQueryId, text) {
  return apiRequest({
    hostname: 'api.telegram.org', path: `/bot${TOKEN}/answerCallbackQuery`,
    method: 'POST', headers: { 'Content-Type': 'application/json' }
  }, { callback_query_id: callbackQueryId, text });
}

function sendTelegram(chatId, text) {
  return apiRequest({
    hostname: 'api.telegram.org', path: `/bot${TOKEN}/sendMessage`,
    method: 'POST', headers: { 'Content-Type': 'application/json' }
  }, { chat_id: chatId, text, parse_mode: 'Markdown' });
}

const server = http.createServer(async (req, res) => {
  if (req.method !== 'POST' || req.url !== '/telegram-callback') {
    res.writeHead(404); res.end('Not found');
    return;
  }

  let body = '';
  req.on('data', chunk => body += chunk);
  req.on('end', async () => {
    try {
      const update = JSON.parse(body);
      const cq = update.callback_query;
      if (!cq) { res.end(JSON.stringify({ ok: false, error: 'no callback_query' })); return; }

      const data = cq.data || '';
      const chatId = cq.from?.id;
      const cqId = cq.id;

      if (!data || !chatId || !cqId) {
        res.end(JSON.stringify({ ok: false, error: 'missing fields' }));
        return;
      }

      const parts = data.split(':');
      if (parts[0] !== 'pick' || parts.length < 3) {
        res.end(JSON.stringify({ ok: false, error: 'invalid format' }));
        return;
      }

      const prescriptionId = parseInt(parts[1]);
      const pharmacyId = parseInt(parts[2]);

      // Get pharmacy name
      let pharmacyName = 'Pharmacie #' + pharmacyId;
      try {
        const pharmResp = await getPharmacies();
        const pharmacies = Array.isArray(pharmResp) ? pharmResp : (pharmResp.value || []);
        const pharm = pharmacies.find(p => p.id === pharmacyId);
        if (pharm) pharmacyName = pharm.pharmacyName;
      } catch(e) {}

      // Assign pharmacy
      let assigned = false;
      try {
        await assignPharmacy(prescriptionId, pharmacyId);
        assigned = true;
      } catch(e) {
        if (e.message && e.message.includes('already assigned')) {
          assigned = false;
        } else {
          assigned = true; // non-blocking for other errors
        }
      }

      if (assigned) {
        await answerCallback(cqId, '✅ Pharmacie sélectionnée !');
        const msg = `✅ *${pharmacyName}* sélectionnée pour l'ordonnance #${prescriptionId}.\n\nVous serez notifié dès que l'ordonnance sera prête.`;
        await sendTelegram(chatId, msg);
      } else {
        await answerCallback(cqId, '⚠️ Une pharmacie est déjà assignée à cette ordonnance.');
        const msg = `⚠️ Une pharmacie a déjà été sélectionnée pour l'ordonnance #${prescriptionId}.\n\nUne seule pharmacie peut être choisie par ordonnance.`;
        await sendTelegram(chatId, msg);
      }

      res.end(JSON.stringify({ ok: true, prescriptionId, pharmacyId, pharmacyName }));
    } catch (e) {
      res.end(JSON.stringify({ ok: false, error: e.message }));
    }
  });
});

server.listen(3456, () => {
  console.log('Telegram callback proxy on :3456/telegram-callback');
});
