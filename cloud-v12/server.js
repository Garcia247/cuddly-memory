import express from 'express';
import crypto from 'node:crypto';
import { promisify } from 'node:util';
import pg from 'pg';

const { Pool } = pg;
const scryptAsync = promisify(crypto.scrypt);
const PORT = Number(process.env.PORT || 3000);
const DATABASE_URL = process.env.DATABASE_URL || '';
const OPENAI_API_KEY = process.env.OPENAI_API_KEY || '';
const MODEL = process.env.OPENAI_MODEL || 'gpt-5.6-terra';
const SESSION_DAYS = 30;

const pool = DATABASE_URL
  ? new Pool({
      connectionString: DATABASE_URL,
      ssl: /localhost|127\.0\.0\.1/.test(DATABASE_URL) ? false : { rejectUnauthorized: false },
      max: 8,
      idleTimeoutMillis: 30000,
    })
  : null;

const app = express();
app.disable('x-powered-by');
app.use(express.json({ limit: '1mb' }));
app.use((req, res, next) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, PATCH, DELETE, OPTIONS');
  if (req.method === 'OPTIONS') return res.status(204).end();
  next();
});

function normalizeEmail(value) {
  return String(value || '').trim().toLowerCase();
}

function sha256(value) {
  return crypto.createHash('sha256').update(value).digest('hex');
}

async function hashPassword(password) {
  const salt = crypto.randomBytes(16);
  const derived = await scryptAsync(password, salt, 64, { N: 16384, r: 8, p: 1 });
  return `scrypt$16384$8$1$${salt.toString('hex')}$${Buffer.from(derived).toString('hex')}`;
}

async function verifyPassword(password, encoded) {
  try {
    const [kind, n, r, p, saltHex, hashHex] = String(encoded).split('$');
    if (kind !== 'scrypt') return false;
    const expected = Buffer.from(hashHex, 'hex');
    const actual = Buffer.from(await scryptAsync(password, Buffer.from(saltHex, 'hex'), expected.length, {
      N: Number(n), r: Number(r), p: Number(p),
    }));
    return expected.length === actual.length && crypto.timingSafeEqual(expected, actual);
  } catch {
    return false;
  }
}

async function issueSession(userId) {
  const raw = crypto.randomBytes(32).toString('base64url');
  const tokenHash = sha256(raw);
  const expires = new Date(Date.now() + SESSION_DAYS * 86400000);
  await pool.query(
    'insert into sessions(user_id, token_hash, expires_at) values ($1,$2,$3)',
    [userId, tokenHash, expires]
  );
  return { token: raw, expiresAt: expires.toISOString() };
}

async function auth(req, res, next) {
  if (!pool) return res.status(503).json({ ok: false, error: 'database_not_connected' });
  const header = String(req.headers.authorization || '');
  if (!header.startsWith('Bearer ')) return res.status(401).json({ ok: false, error: 'authentication_required' });
  const raw = header.slice(7).trim();
  if (!raw) return res.status(401).json({ ok: false, error: 'authentication_required' });
  const result = await pool.query(
    `select u.id, u.email, s.id as session_id
       from sessions s join users u on u.id=s.user_id
      where s.token_hash=$1 and s.expires_at > now()
      limit 1`,
    [sha256(raw)]
  );
  if (!result.rowCount) return res.status(401).json({ ok: false, error: 'session_expired' });
  req.user = result.rows[0];
  req.sessionToken = raw;
  next();
}

function cleanBrain(input = {}) {
  return {
    headline: String(input.headline || '').trim().slice(0, 500),
    services: String(input.services || '').trim().slice(0, 5000),
    evidence: String(input.evidence || '').trim().slice(0, 8000),
    preferredJobs: String(input.preferredJobs || input.preferred_jobs || '').trim().slice(0, 5000),
    avoid: String(input.avoid || '').trim().slice(0, 5000),
    minBudget: Math.max(0, Number.parseInt(input.minBudget ?? input.min_budget ?? 100, 10) || 0),
    hourlyRate: Math.max(0, Number.parseInt(input.hourlyRate ?? input.hourly_rate ?? 15, 10) || 0),
  };
}

async function getBrain(userId) {
  const r = await pool.query('select * from freelancer_brains where user_id=$1', [userId]);
  if (!r.rowCount) return cleanBrain();
  const x = r.rows[0];
  return cleanBrain({
    headline: x.headline,
    services: x.services,
    evidence: x.evidence,
    preferred_jobs: x.preferred_jobs,
    avoid: x.avoid,
    min_budget: x.min_budget,
    hourly_rate: x.hourly_rate,
  });
}

async function putBrain(userId, input) {
  const b = cleanBrain(input);
  await pool.query(
    `insert into freelancer_brains(user_id, headline, services, evidence, preferred_jobs, avoid, min_budget, hourly_rate, updated_at)
     values($1,$2,$3,$4,$5,$6,$7,$8,now())
     on conflict(user_id) do update set
       headline=excluded.headline, services=excluded.services, evidence=excluded.evidence,
       preferred_jobs=excluded.preferred_jobs, avoid=excluded.avoid,
       min_budget=excluded.min_budget, hourly_rate=excluded.hourly_rate, updated_at=now()`,
    [userId, b.headline, b.services, b.evidence, b.preferredJobs, b.avoid, b.minBudget, b.hourlyRate]
  );
  return b;
}

async function getApplications(userId) {
  const r = await pool.query(
    `select id, client_ref as "clientRef", title, description, budget, connects,
            client_details as "clientDetails", opportunity_score as "opportunityScore",
            recommendation, status, proposal, analysis, created_at as "createdAt", updated_at as "updatedAt"
       from applications where user_id=$1 order by updated_at desc limit 500`,
    [userId]
  );
  return r.rows;
}

function cleanStage(stage) {
  const stages = new Set(['Saved','Applied','Reply','Interview','Offer','Won','Lost']);
  return stages.has(stage) ? stage : 'Saved';
}

function analysisSchema() {
  return {
    type: 'object',
    additionalProperties: false,
    properties: {
      opportunityScore: { type: 'integer', minimum: 0, maximum: 100 },
      recommendation: { type: 'string', enum: ['STRONG APPLY','APPLY','CONSIDER','SKIP'] },
      confidence: { type: 'integer', minimum: 0, maximum: 100 },
      clientQuality: { type: 'integer', minimum: 0, maximum: 10 },
      fitSummary: { type: 'string' },
      clientNeed: { type: 'string' },
      winningAngle: { type: 'string' },
      connectsDecision: { type: 'string' },
      suggestedBid: { type: 'integer', minimum: 0 },
      missingEvidence: { type: 'array', items: { type: 'string' }, maxItems: 8 },
      risks: { type: 'array', items: { type: 'string' }, maxItems: 8 },
      bestQuestion: { type: 'string' },
      proposal: { type: 'string' },
    },
    required: [
      'opportunityScore','recommendation','confidence','clientQuality','fitSummary','clientNeed',
      'winningAngle','connectsDecision','suggestedBid','missingEvidence','risks','bestQuestion','proposal'
    ],
  };
}

function extractOutputText(response) {
  if (typeof response.output_text === 'string' && response.output_text) return response.output_text;
  for (const item of response.output || []) {
    for (const content of item.content || []) {
      if ((content.type === 'output_text' || content.type === 'text') && typeof content.text === 'string') return content.text;
    }
  }
  return '';
}

async function openAIAnalyze(brain, job, userId) {
  if (!OPENAI_API_KEY) throw new Error('OPENAI_API_KEY is not configured');
  const instructions = `You are the decision engine for an Upwork application assistant. Analyze one Upwork opportunity for the freelancer profile supplied by the user.\n\nTRUTH GUARD:\n- Never invent credentials, degrees, employers, publications, client results, years of experience, metrics, portfolio items, or tools.\n- You may only assert professional evidence that appears in brain.evidence, brain.services, or brain.headline.\n- If the job expects proof that is not present, put it in missingEvidence and write the proposal around the closest defensible evidence instead.\n- Do not claim the freelancer has completed this exact kind of project unless the supplied evidence says so.\n- Keep the proposal specific to the actual job rather than using a generic template.\n- suggestedBid must be commercially sensible relative to job budget, minimum budget, scope and evidence fit.\n- connectsDecision should be a concise business decision.\n- The proposal should be concise, natural, outcome-oriented and ready for human review. Do not include fabricated social proof.`;

  const payload = {
    model: MODEL,
    instructions,
    input: JSON.stringify({ brain, job }),
    reasoning: { effort: 'medium' },
    text: {
      verbosity: 'low',
      format: {
        type: 'json_schema',
        name: 'upwork_opportunity_analysis',
        strict: true,
        schema: analysisSchema(),
      },
    },
    store: false,
  };

  const response = await fetch('https://api.openai.com/v1/responses', {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${OPENAI_API_KEY}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  });
  const data = await response.json();
  if (!response.ok) {
    const message = data?.error?.message || `OpenAI HTTP ${response.status}`;
    throw new Error(message);
  }
  const text = extractOutputText(data);
  if (!text) throw new Error('OpenAI returned no structured text output');
  const analysis = JSON.parse(text);
  await pool.query(
    'insert into ai_usage(user_id, model, input_tokens, output_tokens) values($1,$2,$3,$4)',
    [userId, MODEL, data?.usage?.input_tokens ?? null, data?.usage?.output_tokens ?? null]
  );
  return analysis;
}

function fallbackAnalyze(brain, job) {
  const hay = `${job.title || ''} ${job.description || ''}`.toLowerCase();
  let score = 46;
  for (const word of ['research','academic','manuscript','journal','thesis','dissertation','literature','spss','methodology','analysis']) {
    if (hay.includes(word)) score += 4;
  }
  const budget = Math.max(0, Number(job.budget) || 0);
  const connects = Math.max(0, Number(job.connects) || 0);
  if (budget >= brain.minBudget) score += 8; else score -= 8;
  if (String(job.clientDetails || '').toLowerCase().includes('verified')) score += 5;
  if (connects <= 12) score += 3;
  if (connects > 20) score -= 8;
  score = Math.max(0, Math.min(95, score));
  return {
    opportunityScore: score,
    recommendation: score >= 88 ? 'STRONG APPLY' : score >= 74 ? 'APPLY' : score >= 60 ? 'CONSIDER' : 'SKIP',
    confidence: 55,
    clientQuality: String(job.clientDetails || '').toLowerCase().includes('verified') ? 8 : 5,
    fitSummary: 'Deterministic fallback based on job language, budget, client details and the saved Freelancer Brain.',
    clientNeed: 'A freelancer who can deliver the requested outcome with clear, defensible evidence of fit.',
    winningAngle: brain.headline || 'Lead with the closest defensible evidence.',
    connectsDecision: score >= 74 && connects <= 16 ? 'Worth spending if the brief is genuine.' : 'Be selective and protect Connects.',
    suggestedBid: Math.max(brain.minBudget, budget > 0 ? Math.round(budget * 0.9) : brain.minBudget),
    missingEvidence: [],
    risks: OPENAI_API_KEY ? ['Live AI was temporarily unavailable; fallback was used.'] : ['Live AI is not configured on the backend.'],
    bestQuestion: 'What is the single most important outcome you want from this engagement?',
    proposal: `I read your brief with the final outcome in mind. My relevant positioning is ${brain.headline || 'research support'}. The closest evidence I can support is ${brain.evidence || 'the experience in my Freelancer Brain'}.\n\nI would begin by identifying the highest-impact requirements, complete the core work against those requirements, and finish with a focused quality-assurance pass.\n\nWhat is the most important outcome you want from this engagement?`,
  };
}

app.get('/', (req, res) => res.json({ ok: true, service: 'AI Upwork Agent Cloud', version: '1.2.0' }));

app.get('/api/status', async (req, res) => {
  let databaseHealthy = false;
  if (pool) {
    try { await pool.query('select 1'); databaseHealthy = true; } catch { databaseHealthy = false; }
  }
  res.json({
    ok: true,
    service: 'AI Upwork Agent Cloud',
    version: '1.2.0',
    ai: Boolean(OPENAI_API_KEY),
    model: MODEL,
    persistence: databaseHealthy ? 'database-connected' : 'database-not-connected',
    auth: databaseHealthy,
  });
});

app.post('/api/auth/register', async (req, res) => {
  if (!pool) return res.status(503).json({ ok: false, error: 'database_not_connected' });
  const email = normalizeEmail(req.body?.email);
  const password = String(req.body?.password || '');
  if (!/^\S+@\S+\.\S+$/.test(email)) return res.status(400).json({ ok: false, error: 'valid_email_required' });
  if (password.length < 8) return res.status(400).json({ ok: false, error: 'password_must_be_at_least_8_characters' });
  try {
    const passwordHash = await hashPassword(password);
    const r = await pool.query(
      'insert into users(email,password_hash) values($1,$2) returning id,email,created_at as "createdAt"',
      [email, passwordHash]
    );
    const user = r.rows[0];
    await putBrain(user.id, req.body?.brain || {});
    const session = await issueSession(user.id);
    return res.status(201).json({ ok: true, user, ...session, brain: await getBrain(user.id), applications: [] });
  } catch (e) {
    if (e?.code === '23505') return res.status(409).json({ ok: false, error: 'email_already_registered' });
    console.error('register', e);
    return res.status(500).json({ ok: false, error: 'registration_failed' });
  }
});

app.post('/api/auth/login', async (req, res) => {
  if (!pool) return res.status(503).json({ ok: false, error: 'database_not_connected' });
  const email = normalizeEmail(req.body?.email);
  const password = String(req.body?.password || '');
  const r = await pool.query('select id,email,password_hash,created_at as "createdAt" from users where email=$1 limit 1', [email]);
  if (!r.rowCount || !(await verifyPassword(password, r.rows[0].password_hash))) {
    return res.status(401).json({ ok: false, error: 'invalid_email_or_password' });
  }
  const user = { id: r.rows[0].id, email: r.rows[0].email, createdAt: r.rows[0].createdAt };
  const session = await issueSession(user.id);
  res.json({ ok: true, user, ...session, brain: await getBrain(user.id), applications: await getApplications(user.id) });
});

app.post('/api/auth/logout', auth, async (req, res) => {
  await pool.query('delete from sessions where id=$1', [req.user.session_id]);
  res.json({ ok: true });
});

app.get('/api/me', auth, async (req, res) => {
  res.json({ ok: true, user: { id: req.user.id, email: req.user.email }, brain: await getBrain(req.user.id), applications: await getApplications(req.user.id) });
});

app.get('/api/brain', auth, async (req, res) => res.json({ ok: true, brain: await getBrain(req.user.id) }));
app.put('/api/brain', auth, async (req, res) => res.json({ ok: true, brain: await putBrain(req.user.id, req.body || {}) }));

app.get('/api/applications', auth, async (req, res) => res.json({ ok: true, applications: await getApplications(req.user.id) }));

app.post('/api/applications', auth, async (req, res) => {
  const a = req.body || {};
  const r = await pool.query(
    `insert into applications(user_id,client_ref,title,description,budget,connects,client_details,opportunity_score,recommendation,status,proposal,analysis,updated_at)
     values($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12::jsonb,now()) returning id`,
    [req.user.id, a.clientRef || null, String(a.title || 'Untitled').slice(0,500), String(a.description || '').slice(0,20000),
     Math.max(0, Number(a.budget)||0), Math.max(0, Number(a.connects)||0), String(a.clientDetails || '').slice(0,10000),
     Math.max(0, Math.min(100, Number(a.opportunityScore)||0)), String(a.recommendation || 'CONSIDER').slice(0,50), cleanStage(a.status),
     String(a.proposal || '').slice(0,30000), JSON.stringify(a.analysis || {})]
  );
  res.status(201).json({ ok: true, id: r.rows[0].id });
});

app.patch('/api/applications/:id', auth, async (req, res) => {
  const status = cleanStage(req.body?.status);
  const r = await pool.query('update applications set status=$1,updated_at=now() where id=$2 and user_id=$3 returning id,status', [status, req.params.id, req.user.id]);
  if (!r.rowCount) return res.status(404).json({ ok: false, error: 'application_not_found' });
  res.json({ ok: true, application: r.rows[0] });
});

app.post('/api/sync/bootstrap', auth, async (req, res) => {
  const client = await pool.connect();
  try {
    await client.query('begin');
    const brain = cleanBrain(req.body?.brain || {});
    await client.query(
      `insert into freelancer_brains(user_id,headline,services,evidence,preferred_jobs,avoid,min_budget,hourly_rate,updated_at)
       values($1,$2,$3,$4,$5,$6,$7,$8,now())
       on conflict(user_id) do update set headline=excluded.headline,services=excluded.services,evidence=excluded.evidence,
       preferred_jobs=excluded.preferred_jobs,avoid=excluded.avoid,min_budget=excluded.min_budget,hourly_rate=excluded.hourly_rate,updated_at=now()`,
      [req.user.id,brain.headline,brain.services,brain.evidence,brain.preferredJobs,brain.avoid,brain.minBudget,brain.hourlyRate]
    );
    for (const a of Array.isArray(req.body?.applications) ? req.body.applications.slice(0,500) : []) {
      await client.query(
        `insert into applications(user_id,client_ref,title,budget,opportunity_score,status,updated_at)
         values($1,$2,$3,$4,$5,$6,now())
         on conflict(user_id,client_ref) where client_ref is not null do update set
           title=excluded.title,budget=excluded.budget,opportunity_score=excluded.opportunity_score,status=excluded.status,updated_at=now()`,
        [req.user.id, a.clientRef || crypto.randomUUID(), String(a.title || 'Untitled').slice(0,500), Math.max(0,Number(a.budget)||0),
         Math.max(0,Math.min(100,Number(a.opportunityScore)||0)), cleanStage(a.status)]
      );
    }
    await client.query('commit');
    res.json({ ok: true, brain: await getBrain(req.user.id), applications: await getApplications(req.user.id) });
  } catch (e) {
    await client.query('rollback');
    console.error('sync', e);
    res.status(500).json({ ok: false, error: 'sync_failed' });
  } finally {
    client.release();
  }
});

app.post('/api/analyze', auth, async (req, res) => {
  const brain = cleanBrain(req.body?.brain || await getBrain(req.user.id));
  const job = req.body?.job || {};
  if (!String(job.title || '').trim() || !String(job.description || '').trim()) {
    return res.status(400).json({ ok: false, error: 'job_title_and_description_required' });
  }
  try {
    const analysis = await openAIAnalyze(brain, job, req.user.id);
    res.json({ ok: true, mode: 'live-ai', model: MODEL, analysis });
  } catch (e) {
    console.error('analysis', e?.message || e);
    res.json({ ok: true, mode: 'fallback', model: MODEL, analysis: fallbackAnalyze(brain, job), aiError: 'live_ai_unavailable' });
  }
});

app.use((err, req, res, next) => {
  console.error('unhandled', err);
  res.status(500).json({ ok: false, error: 'internal_server_error' });
});

app.listen(PORT, '0.0.0.0', () => console.log(`AI Upwork Agent Cloud v1.2 listening on ${PORT}`));
