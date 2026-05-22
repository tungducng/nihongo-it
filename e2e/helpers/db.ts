import { Client } from 'pg'
import bcrypt from 'bcryptjs'

const DB_BASE = {
  host: process.env.E2E_DB_HOST ?? 'localhost',
  port: Number(process.env.E2E_DB_PORT ?? 5433),
  user: process.env.E2E_DB_USER ?? 'postgres',
  password: process.env.E2E_DB_PASSWORD ?? 'NihongoIT2024!',
}

const DB_NAMES = {
  user: process.env.E2E_USER_SERVICE_DB ?? 'user_service',
  learning: process.env.E2E_LEARNING_SERVICE_DB ?? 'learning_service',
  notification: process.env.E2E_NOTIFICATION_SERVICE_DB ?? 'notification_service',
} as const

export type ServiceDb = keyof typeof DB_NAMES

export async function withClient<T>(
  service: ServiceDb,
  fn: (client: Client) => Promise<T>,
): Promise<T> {
  const client = new Client({ ...DB_BASE, database: DB_NAMES[service] })
  await client.connect()
  try {
    return await fn(client)
  } finally {
    await client.end()
  }
}

// Truncate test-owned tables WITHOUT touching the seed users.
// Called by globalTeardown and between suites if needed.
export async function truncateLearningOwned(): Promise<void> {
  await withClient('learning', async (c) => {
    await c.query(`
      TRUNCATE TABLE
        review_logs,
        saved_vocabulary,
        flashcards,
        feedback,
        user_progress
      RESTART IDENTITY CASCADE
    `)
  })
}

// Mark a user as ADMIN. Used after signup to elevate the test admin account.
export async function promoteToAdmin(email: string): Promise<void> {
  await withClient('user', async (c) => {
    await c.query(
      `UPDATE users
       SET role_id = (SELECT role_id FROM roles WHERE role_name ='ROLE_ADMIN'),
           is_email_verified = true,
           is_active = true
       WHERE email = $1`,
      [email],
    )
  })
}

// Mark a user as verified (so login allows / OAuth flows pass).
export async function markVerified(email: string): Promise<void> {
  await withClient('user', async (c) => {
    await c.query(
      `UPDATE users SET is_email_verified = true, is_active = true WHERE email = $1`,
      [email],
    )
  })
}

export async function deleteUserByEmail(email: string): Promise<void> {
  await withClient('user', async (c) => {
    await c.query(`DELETE FROM users WHERE email = $1`, [email])
  })
}

// Idempotent user upsert via direct SQL. Bypasses the HTTP signup flow because
// the gateway/devtools/OTel combo currently corrupts the response stream.
// Spring Security uses BCrypt with cost 10 by default — match that.
export async function upsertUser(opts: {
  email: string
  password: string
  fullName: string
  isAdmin?: boolean
}): Promise<void> {
  const hash = await bcrypt.hash(opts.password, 10)
  const roleName = opts.isAdmin ? 'ROLE_ADMIN' : 'ROLE_USER'
  await withClient('user', async (c) => {
    await c.query(
      `INSERT INTO users (
         user_id, email, password, full_name,
         current_level, jlpt_goal, is_active, is_email_verified,
         reminder_enabled, role_id,
         created_at, updated_at, created_by, updated_by
       )
       VALUES (
         gen_random_uuid(), $1, $2, $3,
         'N5', 'N3', true, true,
         true, (SELECT role_id FROM roles WHERE role_name =$4),
         NOW(), NOW(), 'e2e-seed', 'e2e-seed'
       )
       ON CONFLICT (email) DO UPDATE SET
         password = EXCLUDED.password,
         is_active = true,
         is_email_verified = true,
         role_id = EXCLUDED.role_id,
         updated_at = NOW(),
         updated_by = 'e2e-seed'`,
      [opts.email, hash, opts.fullName, roleName],
    )
  })
}
