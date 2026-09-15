# Upwork Workflow Cloud — Railway deployment contract

## Service
- Repository: `Garcia247/cuddly-memory`
- Branch during staging: `upwork-workflow-v13`
- Root directory: `/cloud-v12`
- Builder: Dockerfile (auto-detected from the service root)
- Start: Dockerfile runs `npm run migrate && npm start`
- Health endpoint: `/api/status`

## Database
Attach one Railway PostgreSQL service and expose its `DATABASE_URL` to the backend service. The backend runs `schema.sql` idempotently at startup before accepting traffic.

## Required variables
- `DATABASE_URL` — supplied by the attached PostgreSQL service
- `OPENAI_API_KEY` — server-side only; never place this value in the Android app or repository
- `OPENAI_MODEL` — optional, defaults to `gpt-5.6-terra`
- `NODE_ENV=production`

## Expected status response
`GET /api/status` must return HTTP 200 with:
- `ok: true`
- `persistence: "database-connected"`
- `auth: true`
- `ai: true` once the OpenAI key is configured

## Acceptance tests before changing the APK API base URL
1. `/api/status` is publicly reachable over HTTPS.
2. Register a throwaway test account through `/api/auth/register`.
3. Authenticate through `/api/auth/login`.
4. Save and retrieve Freelancer Brain data.
5. Save and retrieve an application record.
6. `/api/analyze` returns a structured analysis and truth guard does not invent evidence.
7. Restart the backend and confirm account/application data persists.
8. Only after all tests pass, point the Android production build to the Railway public domain.

## Security rules
- Never expose `DATABASE_URL` or `OPENAI_API_KEY` to the Android client.
- Keep sessions opaque and hashed server-side.
- Keep password hashes server-side only.
- Production Android builds must use HTTPS only.
- Human approval remains required before any future Upwork write/submission action.
