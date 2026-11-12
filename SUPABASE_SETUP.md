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

## Contact inbox and email replies

The contact form saves visitor messages in a private inbox. The web and Android managers load messages when the Inbox is refreshed. Replies are stored in the inbox and sent to the visitor by email through Resend.

### Deploy the inbox database and Edge Functions

The migration and functions live in `supabase/`. Use the Supabase CLI from the repository root and link it to the same project as `src/app/services/supabase.config.ts`:

```powershell
supabase login
supabase link --project-ref YOUR_PROJECT_REF
supabase db push
supabase functions deploy contact-message --no-verify-jwt
supabase functions deploy send-contact-reply
```

The migration restricts inbox rows, replies, and read state to the configured portfolio owner UUID, and adds an atomic limit of five submissions per IP per hour. Change the owner UUID in the migration policies if it differs from `SUPABASE_ADMIN_USER_ID`.

### Configure email secrets

Verify a sender domain in [Resend](https://resend.com/), then create an API key.

Create `supabase/functions/.env` locally (it is Git-ignored) with the following values:

```dotenv
SUPABASE_ADMIN_USER_ID=your-owner-user-uuid
SUPABASE_ANON_KEY=your-public-anon-or-publishable-key
ALLOWED_ORIGINS=https://your-portfolio-domain.example,http://localhost:4200
CONTACT_RATE_LIMIT_SALT=replace-with-a-random-secret-at-least-32-characters
RESEND_API_KEY=re_your_resend_key
EMAIL_FROM="Mahak Portfolio <notifications@your-verified-domain.example>"
OWNER_EMAIL=your-reply-address@example.com
```

Deploy these values as Supabase Edge Function secrets (do not commit the `.env` file):

```powershell
supabase secrets set --env-file supabase/functions/.env --project-ref YOUR_PROJECT_REF
```

`ALLOWED_ORIGINS` must contain the exact website origins, comma-separated, with no trailing slash. Include your deployed website and local development URL. The contact endpoint validates inputs, checks a hidden spam field, and rate-limits submissions. The server-side service-role key is provided by Supabase and must never be added to client code.

New messages appear in the web and Android inbox after refreshing. If Resend setup is missing, messages remain stored in the inbox; email delivery errors are recorded in Supabase Function logs.
