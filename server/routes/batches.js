const express = require('express');
const router = express.Router();
const db = require('../db');
const { v4: uuidv4 } = require('uuid');

const MACHINE_ID = 'local-web-server';

router.get('/', (req, res) => {
  try {
    const batches = db.prepare('SELECT * FROM batch WHERE deleted_at IS NULL ORDER BY created_at DESC').all();
    res.json(batches);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.get('/:id', (req, res) => {
  try {
    const batch = db.prepare('SELECT * FROM batch WHERE id = ?').get(req.params.id);
    if (!batch) return res.status(404).json({ error: '未找到批次' });
    res.json(batch);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.post('/', (req, res) => {
  const { name, destination, remark } = req.body;
  const now = new Date().toISOString();
  const id = uuidv4();

  try {
    db.prepare(`
      INSERT INTO batch (id, name, destination, remark, created_at, updated_at, machine_id)
      VALUES (?, ?, ?, ?, ?, ?, ?)
    `).run(id, name, destination || null, remark || null, now, now, MACHINE_ID);
    res.status(201).json({ id, name, destination, remark });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.delete('/:id', (req, res) => {
  const now = new Date().toISOString();
  try {
    db.prepare('UPDATE batch SET deleted_at = ?, updated_at = ? WHERE id = ?').run(now, now, req.params.id);
    res.json({ success: true });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
