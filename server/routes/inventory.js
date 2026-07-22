const express = require('express');
const router = express.Router();
const db = require('../db');
const { v4: uuidv4 } = require('uuid');

const MACHINE_ID = 'local-web-server'; // In a real app, this should be unique per machine

// Get all products
router.get('/', (req, res) => {
  const { search } = req.query;
  let query = 'SELECT * FROM product_inventory WHERE deleted_at IS NULL';
  const params = [];

  if (search) {
    query += ' AND (name LIKE ? OR product_code LIKE ? OR barcode LIKE ?)';
    const searchPattern = `%${search}%`;
    params.push(searchPattern, searchPattern, searchPattern);
  }

  query += ' ORDER BY created_at DESC';

  try {
    const products = db.prepare(query).all(params);
    res.json(products);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Create product
router.post('/', (req, res) => {
  const { product_code, name, barcode, length_cm, width_cm, height_cm, weight_g, custom_specs } = req.body;
  const now = new Date().toISOString();
  const id = uuidv4();

  try {
    const info = db.prepare(`
      INSERT INTO product_inventory (id, product_code, name, barcode, length_cm, width_cm, height_cm, weight_g, custom_specs, created_at, updated_at, machine_id)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `).run(id, product_code || null, name, barcode || null, length_cm, width_cm, height_cm, weight_g, custom_specs || null, now, now, MACHINE_ID);

    res.status(201).json({ id, ...req.body });
  } catch (err) {
    if (err.message.includes('UNIQUE constraint failed: product_inventory.product_code')) {
      return res.status(400).json({ error: '产品编码已存在' });
    }
    res.status(500).json({ error: err.message });
  }
});

// Get single product
router.get('/:id', (req, res) => {
  try {
    const product = db.prepare('SELECT * FROM product_inventory WHERE id = ?').get(req.params.id);
    if (!product) return res.status(404).json({ error: '未找到产品' });
    res.json(product);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Update product
router.put('/:id', (req, res) => {
  const { product_code, name, barcode, length_cm, width_cm, height_cm, weight_g, custom_specs } = req.body;
  const now = new Date().toISOString();

  try {
    const info = db.prepare(`
      UPDATE product_inventory
      SET product_code = ?, name = ?, barcode = ?, length_cm = ?, width_cm = ?, height_cm = ?, weight_g = ?, custom_specs = ?, updated_at = ?
      WHERE id = ?
    `).run(product_code || null, name, barcode || null, length_cm, width_cm, height_cm, weight_g, custom_specs || null, now, req.params.id);

    if (info.changes === 0) return res.status(404).json({ error: '未找到产品' });
    res.json({ id: req.params.id, ...req.body });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Delete product (soft delete)
router.delete('/:id', (req, res) => {
  const now = new Date().toISOString();
  try {
    const info = db.prepare('UPDATE product_inventory SET deleted_at = ?, updated_at = ? WHERE id = ?')
      .run(now, now, req.params.id);
    if (info.changes === 0) return res.status(404).json({ error: '未找到产品' });
    res.json({ success: true });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
