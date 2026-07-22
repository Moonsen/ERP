const Database = require('better-sqlite3');
const path = require('path');
const { v4: uuidv4 } = require('uuid');

const dbPath = path.resolve(__dirname, 'warehouse.db');
const db = new Database(dbPath);

// Initialize schema
db.exec(`
  CREATE TABLE IF NOT EXISTS batch (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    destination TEXT,
    remark TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT,
    machine_id TEXT NOT NULL
  );

  CREATE TABLE IF NOT EXISTS box (
    id TEXT PRIMARY KEY,
    batch_id TEXT NOT NULL,
    box_number INTEGER NOT NULL,
    length_cm REAL NOT NULL,
    width_cm REAL NOT NULL,
    height_cm REAL NOT NULL,
    weight_kg REAL NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT,
    machine_id TEXT NOT NULL,
    FOREIGN KEY (batch_id) REFERENCES batch(id)
  );

  CREATE TABLE IF NOT EXISTS product_inventory (
    id TEXT PRIMARY KEY,
    product_code TEXT UNIQUE,
    name TEXT NOT NULL,
    barcode TEXT,
    length_cm REAL NOT NULL,
    width_cm REAL NOT NULL,
    height_cm REAL NOT NULL,
    weight_g REAL NOT NULL,
    custom_specs TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT,
    machine_id TEXT NOT NULL
  );

  CREATE TABLE IF NOT EXISTS box_product (
    id TEXT PRIMARY KEY,
    box_id TEXT NOT NULL,
    inventory_id TEXT,
    product_number INTEGER NOT NULL,
    name TEXT NOT NULL,
    barcode TEXT,
    length_cm REAL NOT NULL,
    width_cm REAL NOT NULL,
    height_cm REAL NOT NULL,
    weight_g REAL NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT,
    machine_id TEXT NOT NULL,
    FOREIGN KEY (box_id) REFERENCES box(id),
    FOREIGN KEY (inventory_id) REFERENCES product_inventory(id)
  );

  CREATE INDEX IF NOT EXISTS idx_inventory_barcode ON product_inventory(barcode);
  CREATE INDEX IF NOT EXISTS idx_inventory_code ON product_inventory(product_code);
  CREATE INDEX IF NOT EXISTS idx_inventory_updated ON product_inventory(updated_at);
  CREATE INDEX IF NOT EXISTS idx_box_product_box ON box_product(box_id, product_number);
  CREATE INDEX IF NOT EXISTS idx_box_product_inventory ON box_product(inventory_id);
  CREATE INDEX IF NOT EXISTS idx_box_product_updated ON box_product(updated_at);
  CREATE INDEX IF NOT EXISTS idx_batch_updated ON batch(updated_at);
  CREATE INDEX IF NOT EXISTS idx_box_updated ON box(updated_at);

  CREATE TABLE IF NOT EXISTS config (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL
  );
`);

module.exports = db;
