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

Visitors can send messages through the portfolio contact form. Each message is saved to a private inbox that only the portfolio owner can access from the web or Android manager. The inbox does not send push notifications or update automatically; open the Inbox tab and select **Refresh inbox** to check for new messages.

You can reply to a visitor from either manager. Replies are saved with the original message and emailed to the visitor through Resend.

### Deploy the inbox database and Edge Functions

The migration and functions live in `supabase/`. In PowerShell, run these commands from the repository root. They use `npx` to download and run the Supabase CLI, so a global `supabase` command is not required. Node.js and npm must be installed.

```powershell
npx supabase login
npx supabase link --project-ref YOUR_PROJECT_REF
npx supabase db push --remote
npx supabase functions deploy contact-message --no-verify-jwt
npx supabase functions deploy send-contact-reply
```

The `--remote` flag applies migrations to the linked Supabase project instead of connecting to a local Supabase stack. Confirm the displayed project reference before accepting the migration prompt.

The migration restricts inbox rows, replies, and read state to the configured portfolio owner UUID, and adds an atomic limit of five submissions per IP per hour. Change the owner UUID in the migration policies if it differs from `ADMIN_USER_ID`.

### Configure email secrets

Verify a sender domain in [Resend](https://resend.com/), then create an API key.

The local, Git-ignored `supabase/functions/.env` is set up with the owner ID, public Supabase key, wildcard origin (`ALLOWED_ORIGINS=*`), and a generated rate-limit salt. The wildcard permits browser requests from any website. For tighter access, replace `*` with the exact comma-separated website origins. Add your Resend API key, verified sender, and reply address to enable email replies:

```dotenv
RESEND_API_KEY=re_your_resend_key
EMAIL_FROM="Mahak Portfolio <notifications@your-verified-domain.example>"
OWNER_EMAIL=your-reply-address@example.com
```

Supabase automatically provides `SUPABASE_URL` and `SUPABASE_SERVICE_ROLE_KEY` to deployed Edge Functions. The functions also accept a custom `SERVICE_ROLE_KEY` secret if you have already added it. Never expose the service-role key in frontend code. For local function testing, Supabase CLI supplies its local service-role key when running:

```powershell
npx supabase functions serve --env-file supabase/functions/.env
```

After filling in the live website origin and Resend values, deploy the configured variables as Supabase Edge Function secrets:

```powershell
npx supabase secrets set --env-file supabase/functions/.env --project-ref YOUR_PROJECT_REF
```

The contact endpoint validates inputs, checks a hidden spam field, and rate-limits submissions. Wildcard CORS does not authenticate callers; the service-role key must remain server-side.

New messages appear in the web and Android inbox after you refresh it. If Resend is not configured or an email cannot be delivered, the message and reply remain saved in the inbox; check Supabase Function logs for delivery errors.
