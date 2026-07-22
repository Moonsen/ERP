const express = require('express');
const router = express.Router();
const db = require('../db');
const multer = require('multer');
const upload = multer();

const MACHINE_ID = 'local-web-server';

// Export backup
router.get('/export', (req, res) => {
  try {
    const batches = db.prepare('SELECT * FROM batch WHERE deleted_at IS NULL').all();
    const boxes = db.prepare('SELECT * FROM box WHERE deleted_at IS NULL').all();
    const productInventory = db.prepare('SELECT * FROM product_inventory WHERE deleted_at IS NULL').all();
    const boxProducts = db.prepare('SELECT * FROM box_product WHERE deleted_at IS NULL').all();

    const backup = {
      version: '3.1',
      exportTime: new Date().toISOString(),
      exportMachine: MACHINE_ID,
      recordCount: batches.length + boxes.length + productInventory.length + boxProducts.length,
      batches,
      boxes,
      productInventory,
      boxProducts
    };

    res.setHeader('Content-Type', 'application/json');
    res.setHeader('Content-Disposition', `attachment; filename="warehouse-backup-${new Date().toISOString().slice(0, 19).replace(/:/g, '-')}.json"`);
    res.json(backup);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Restore backup
router.post('/restore', upload.single('file'), (req, res) => {
  if (!req.file) return res.status(400).json({ error: '未上传文件' });

  let backupData;
  try {
    backupData = JSON.parse(req.file.buffer.toString('utf-8'));
  } catch (err) {
    return res.status(400).json({ error: '文件格式错误' });
  }

  const requiredArrays = ['batches', 'boxes', 'productInventory', 'boxProducts'];
  for (const key of requiredArrays) {
    if (!Array.isArray(backupData[key])) {
      return res.status(400).json({ error: `备份文件缺少 ${key} 数组` });
    }
  }

  const transaction = db.transaction(() => {
    db.prepare('DELETE FROM box_product').run();
    db.prepare('DELETE FROM product_inventory').run();
    db.prepare('DELETE FROM box').run();
    db.prepare('DELETE FROM batch').run();

    const insertBatch = db.prepare('INSERT INTO batch (id, name, destination, remark, created_at, updated_at, deleted_at, machine_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)');
    for (const b of backupData.batches) insertBatch.run(b.id, b.name, b.destination, b.remark, b.created_at, b.updated_at, b.deleted_at, b.machine_id);

    const insertBox = db.prepare('INSERT INTO box (id, batch_id, box_number, length_cm, width_cm, height_cm, weight_kg, created_at, updated_at, deleted_at, machine_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)');
    for (const b of backupData.boxes) insertBox.run(b.id, b.batch_id, b.box_number, b.length_cm, b.width_cm, b.height_cm, b.weight_kg, b.created_at, b.updated_at, b.deleted_at, b.machine_id);

    const insertInv = db.prepare('INSERT INTO product_inventory (id, product_code, name, barcode, length_cm, width_cm, height_cm, weight_g, custom_specs, created_at, updated_at, deleted_at, machine_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)');
    for (const i of backupData.productInventory) insertInv.run(i.id, i.product_code, i.name, i.barcode, i.length_cm, i.width_cm, i.height_cm, i.weight_g, i.custom_specs, i.created_at, i.updated_at, i.deleted_at, i.machine_id);

    const insertBoxProd = db.prepare('INSERT INTO box_product (id, box_id, inventory_id, product_number, name, barcode, length_cm, width_cm, height_cm, weight_g, quantity, created_at, updated_at, deleted_at, machine_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)');
    for (const p of backupData.boxProducts) insertBoxProd.run(p.id, p.box_id, p.inventory_id, p.product_number, p.name, p.barcode, p.length_cm, p.width_cm, p.height_cm, p.weight_g, p.quantity, p.created_at, p.updated_at, p.deleted_at, p.machine_id);
  });

  try {
    transaction();
    res.json({ success: true, message: '恢复成功' });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
