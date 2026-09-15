import fs from 'node:fs/promises';
import pg from 'pg';

const { Pool } = pg;
const DATABASE_URL = process.env.DATABASE_URL || '';

if (!DATABASE_URL) {
  console.error('DATABASE_URL is required for migrations');
  process.exit(1);
}

const pool = new Pool({
  connectionString: DATABASE_URL,
  ssl: /localhost|127\.0\.0\.1/.test(DATABASE_URL) ? false : { rejectUnauthorized: false },
  max: 2,
});

try {
  const sql = await fs.readFile(new URL('./schema.sql', import.meta.url), 'utf8');
  await pool.query(sql);
  console.log('Database schema is ready.');
} catch (error) {
  console.error('Migration failed:', error);
  process.exitCode = 1;
} finally {
  await pool.end();
}
