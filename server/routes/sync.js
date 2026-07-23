const express = require('express');
const router = express.Router();
const db = require('../db');
const syncService = require('../sync/sync-service');
const webdavClient = require('../sync/webdav-client');

// Get current config
router.get('/config', (req, res) => {
  try {
    const config = db.prepare('SELECT * FROM config').all();
    const configMap = {};
    config.forEach(item => {
      if (item.key === 'webdav_password') {
        configMap[item.key] = '********'; // Don't send password back plainly
      } else {
        configMap[item.key] = item.value;
      }
    });
    res.json(configMap);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Save config
router.post('/config', async (req, res) => {
  const { url, username, password } = req.body;
  try {
    const upsert = db.prepare('INSERT INTO config (key, value) VALUES (?, ?) ON CONFLICT(key) DO UPDATE SET value = excluded.value');

    upsert.run('webdav_url', url);
    upsert.run('webdav_username', username);
    if (password && password !== '********') {
      upsert.run('webdav_password', password);
    }

    // Re-initialize client
    await webdavClient.initClient(url, username, (password && password !== '********') ? password : getStoredPassword());

    res.json({ success: true, message: '配置已保存' });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

function getStoredPassword() {
  const row = db.prepare('SELECT value FROM config WHERE key = ?').get('webdav_password');
  return row ? row.value : '';
}

router.post('/start', async (req, res) => {
  try {
    // Ensure client is initialized from DB if not already
    if (!webdavClient.isInitialized()) {
      const url = db.prepare('SELECT value FROM config WHERE key = ?').get('webdav_url')?.value;
      const username = db.prepare('SELECT value FROM config WHERE key = ?').get('webdav_username')?.value;
      const password = db.prepare('SELECT value FROM config WHERE key = ?').get('webdav_password')?.value;

      if (url && username && password) {
        webdavClient.initClient(url, username, password);
      } else {
        return res.status(400).json({ error: '请先配置 WebDAV' });
      }
    }

    const result = await syncService.performSync();
    res.json(result);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
