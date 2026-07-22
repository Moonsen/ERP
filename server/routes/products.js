const express = require('express');
const router = express.Router();
const db = require('../db');
const { v4: uuidv4 } = require('uuid');

const MACHINE_ID = 'local-web-server';

// Get products in a box
router.get('/:boxId', (req, res) => {
  try {
    const products = db.prepare('SELECT * FROM box_product WHERE box_id = ? AND deleted_at IS NULL ORDER BY product_number ASC')
      .all(req.params.boxId);
    res.json(products);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Add product to box
router.post('/', (req, res) => {
  const { box_id, inventory_id, name, barcode, length_cm, width_cm, height_cm, weight_g, quantity } = req.body;
  const now = new Date().toISOString();
  const id = uuidv4();

  const transaction = db.transaction(() => {
    let finalInventoryId = inventory_id;

    if (!inventory_id) {
      // Manual entry: auto-create inventory record
      finalInventoryId = uuidv4();
      db.prepare(`
        INSERT INTO product_inventory (id, name, barcode, length_cm, width_cm, height_cm, weight_g, custom_specs, created_at, updated_at, machine_id)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      `).run(finalInventoryId, name, barcode || null, length_cm, width_cm, height_cm, weight_g, null, now, now, MACHINE_ID);
    }

    const maxProd = db.prepare('SELECT MAX(product_number) as maxNum FROM box_product WHERE box_id = ? AND deleted_at IS NULL')
      .get(box_id);
    const product_number = (maxProd.maxNum || 0) + 1;

    db.prepare(`
      INSERT INTO box_product (id, box_id, inventory_id, product_number, name, barcode, length_cm, width_cm, height_cm, weight_g, quantity, created_at, updated_at, machine_id)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `).run(id, box_id, finalInventoryId, product_number, name, barcode || null, length_cm, width_cm, height_cm, weight_g, quantity, now, now, MACHINE_ID);

    return { id, product_number, inventory_id: finalInventoryId };
  });

  try {
    const result = transaction();
    res.status(201).json({ ...result, ...req.body });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.delete('/:id', (req, res) => {
  const now = new Date().toISOString();
  try {
    db.prepare('UPDATE box_product SET deleted_at = ?, updated_at = ? WHERE id = ?').run(now, now, req.params.id);
    res.json({ success: true });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
