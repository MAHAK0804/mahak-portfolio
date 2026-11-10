# Shared Portfolio Content Setup

The portfolio currently uses local defaults until Supabase is configured. Supabase stores one shared content record: visitors can read it, while only the configured owner account can save edits.

## Configure Supabase

1. Create a Supabase project.
2. In Authentication, create the owner account with an email and password. Copy its user UUID. Disable public sign-ups after creating this account.
3. In `src/app/services/supabase.config.ts`, set the project URL, the project's public anon/publishable key, and the owner UUID. Never put a `service_role` key in this frontend app.
4. Open the Supabase SQL Editor, replace both `OWNER_USER_UUID` values below with the owner UUID, and run the SQL:

```sql
create table public.portfolio_content (
  id smallint primary key check (id = 1),
  owner_id uuid not null,
  content jsonb not null,
  updated_at timestamptz not null default now()
);

alter table public.portfolio_content enable row level security;

grant select on public.portfolio_content to anon, authenticated;
grant insert, update on public.portfolio_content to authenticated;

create policy "Portfolio content is public"
  on public.portfolio_content
  for select
  to anon, authenticated
  using (true);

create policy "Only owner can insert portfolio content"
  on public.portfolio_content
  for insert
  to authenticated
  with check (
    auth.uid() = owner_id
    and auth.uid() = '94cec00f-fcca-464a-81cf-39c7256271ca'::uuid
  );

create policy "Only owner can update portfolio content"
  on public.portfolio_content
  for update
  to authenticated
  using (
    auth.uid() = owner_id
    and auth.uid() = '94cec00f-fcca-464a-81cf-39c7256271ca'::uuid
  )
  with check (
    auth.uid() = owner_id
    and auth.uid() = '94cec00f-fcca-464a-81cf-39c7256271ca'::uuid
  );
```

5. Rebuild and deploy the app. Sign in at `/manage`. The first successful save creates the shared row; subsequent visits from any device load that row for the portfolio.

The anon key is intended for browser use. Row-level security is what protects writes, so keep the owner UUID consistent in the app config and both SQL policies. Do not remove the owner check or expose a service-role key.
