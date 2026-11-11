const Database = require('better-sqlite3');
const db = new Database('C:\\Users\\asus\\.n8n\\database.sqlite', { readonly: true });
const webhooks = db.prepare("SELECT * FROM webhook_entity").all();
console.log('Webhooks:', JSON.stringify(webhooks, null, 2));