create extension if not exists pgcrypto;

create table if not exists users (
  id uuid primary key default gen_random_uuid(),
  email text not null unique,
  password_hash text not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists sessions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references users(id) on delete cascade,
  token_hash text not null unique,
  expires_at timestamptz not null,
  created_at timestamptz not null default now()
);

create index if not exists sessions_user_idx on sessions(user_id);
create index if not exists sessions_expires_idx on sessions(expires_at);

create table if not exists freelancer_brains (
  user_id uuid primary key references users(id) on delete cascade,
  headline text not null default '',
  services text not null default '',
  evidence text not null default '',
  preferred_jobs text not null default '',
  avoid text not null default '',
  min_budget integer not null default 100 check (min_budget >= 0),
  hourly_rate integer not null default 15 check (hourly_rate >= 0),
  updated_at timestamptz not null default now()
);

create table if not exists applications (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references users(id) on delete cascade,
  client_ref text,
  title text not null,
  description text not null default '',
  budget integer not null default 0,
  connects integer not null default 0,
  client_details text not null default '',
  opportunity_score integer not null default 0 check (opportunity_score between 0 and 100),
  recommendation text not null default 'CONSIDER',
  status text not null default 'Saved' check (status in ('Saved','Applied','Reply','Interview','Offer','Won','Lost')),
  proposal text not null default '',
  analysis jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index if not exists applications_user_updated_idx on applications(user_id, updated_at desc);
create unique index if not exists applications_user_client_ref_uniq
  on applications(user_id, client_ref)
  where client_ref is not null;

create table if not exists ai_usage (
  id bigserial primary key,
  user_id uuid not null references users(id) on delete cascade,
  model text not null,
  input_tokens integer,
  output_tokens integer,
  created_at timestamptz not null default now()
);

create index if not exists ai_usage_user_date_idx on ai_usage(user_id, created_at desc);
