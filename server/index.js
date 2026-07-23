const express = require('express');
const cors = require('cors');
const db = require('./db');
const { v4: uuidv4 } = require('uuid');

const path = require('path');

const app = express();
const port = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

// Routes
const inventoryRoutes = require('./routes/inventory');
const batchRoutes = require('./routes/batches');
const boxRoutes = require('./routes/boxes');
const productRoutes = require('./routes/products');
const backupRoutes = require('./routes/backup');
const syncRoutes = require('./routes/sync');

// Auto-initialize WebDAV if config exists
const webdavClient = require('./sync/webdav-client');
(async () => {
  try {
    const url = db.prepare('SELECT value FROM config WHERE key = ?').get('webdav_url')?.value;
    const username = db.prepare('SELECT value FROM config WHERE key = ?').get('webdav_username')?.value;
    const password = db.prepare('SELECT value FROM config WHERE key = ?').get('webdav_password')?.value;

    if (url && username && password) {
      await webdavClient.initClient(url, username, password);
      console.log('WebDAV client auto-initialized from database');
    }
  } catch (err) {
    console.error('Failed to auto-initialize WebDAV:', err.message);
  }
})();

app.use('/api/inventory', inventoryRoutes);
app.use('/api/batches', batchRoutes);
app.use('/api/boxes', boxRoutes);
app.use('/api/box-products', productRoutes);
app.use('/api/backup', backupRoutes);
app.use('/api/sync', syncRoutes);

// Static files (Frontend)
app.use(express.static(path.join(__dirname, '../client/dist')));

// Fallback to index.html for React Router
app.get('*', (req, res) => {
  if (!req.path.startsWith('/api/')) {
    res.sendFile(path.join(__dirname, '../client/dist/index.html'));
  }
});

app.listen(port, () => {
  console.log(`Server running at http://localhost:${port}`);
});
