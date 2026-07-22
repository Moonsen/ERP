const express = require('express');
const router = express.Router();
const db = require('../db');
const { v4: uuidv4 } = require('uuid');

const MACHINE_ID = 'local-web-server';

router.get('/all', (req, res) => {
  try {
    const boxes = db.prepare('SELECT * FROM box WHERE deleted_at IS NULL').all();
    res.json(boxes);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.get('/single/:id', (req, res) => {
  try {
    const box = db.prepare('SELECT * FROM box WHERE id = ?').get(req.params.id);
    if (!box) return res.status(404).json({ error: '未找到箱子' });
    res.json(box);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.get('/:batchId', (req, res) => {
  try {
    const boxes = db.prepare('SELECT * FROM box WHERE batch_id = ? AND deleted_at IS NULL ORDER BY box_number ASC')
      .all(req.params.batchId);
    res.json(boxes);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.post('/', (req, res) => {
  const { batch_id, length_cm, width_cm, height_cm, weight_kg } = req.body;
  const now = new Date().toISOString();
  const id = uuidv4();

  try {
    const maxBox = db.prepare('SELECT MAX(box_number) as maxNum FROM box WHERE batch_id = ? AND deleted_at IS NULL')
      .get(batch_id);
    const box_number = (maxBox.maxNum || 0) + 1;

    db.prepare(`
      INSERT INTO box (id, batch_id, box_number, length_cm, width_cm, height_cm, weight_kg, created_at, updated_at, machine_id)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `).run(id, batch_id, box_number, length_cm, width_cm, height_cm, weight_kg, now, now, MACHINE_ID);

    res.status(201).json({ id, box_number, ...req.body });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.delete('/:id', (req, res) => {
  const now = new Date().toISOString();
  try {
    db.prepare('UPDATE box SET deleted_at = ?, updated_at = ? WHERE id = ?').run(now, now, req.params.id);
    res.json({ success: true });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
