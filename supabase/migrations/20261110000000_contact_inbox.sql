create table public.contact_messages (
  id uuid primary key default gen_random_uuid(),
  full_name text not null check (char_length(btrim(full_name)) between 1 and 120),
  email text not null check (char_length(email) between 3 and 320),
  subject text not null default '' check (char_length(subject) <= 200),
  message text not null check (char_length(btrim(message)) between 1 and 5000),
  created_at timestamptz not null default now(),
  is_read boolean not null default false
);

create index contact_messages_created_at_idx
  on public.contact_messages (created_at desc);

create table public.contact_message_replies (
  id uuid primary key default gen_random_uuid(),
  message_id uuid not null references public.contact_messages (id) on delete cascade,
  body text not null check (char_length(btrim(body)) between 1 and 5000),
  created_at timestamptz not null default now(),
  email_sent_at timestamptz
);

create index contact_message_replies_message_id_created_at_idx
  on public.contact_message_replies (message_id, created_at);

alter table public.contact_messages enable row level security;
alter table public.contact_message_replies enable row level security;

revoke all on public.contact_messages, public.contact_message_replies
  from anon, authenticated;

grant select on public.contact_messages to authenticated;
grant update (is_read) on public.contact_messages to authenticated;
grant select, insert on public.contact_message_replies to authenticated;
grant select, insert, update, delete on public.contact_messages, public.contact_message_replies to service_role;
create policy "Portfolio owner can read contact messages"
  on public.contact_messages for select to authenticated
  using (auth.uid() = '94cec00f-fcca-464a-81cf-39c7256271ca'::uuid);

create policy "Portfolio owner can mark contact messages read"
  on public.contact_messages for update to authenticated
  using (auth.uid() = '94cec00f-fcca-464a-81cf-39c7256271ca'::uuid)
  with check (auth.uid() = '94cec00f-fcca-464a-81cf-39c7256271ca'::uuid);

create policy "Portfolio owner can read contact replies"
  on public.contact_message_replies for select to authenticated
  using (
    auth.uid() = '94cec00f-fcca-464a-81cf-39c7256271ca'::uuid
    and exists (
      select 1 from public.contact_messages
      where contact_messages.id = contact_message_replies.message_id
    )
  );

create policy "Portfolio owner can write contact replies"
  on public.contact_message_replies for insert to authenticated
  with check (
    auth.uid() = '94cec00f-fcca-464a-81cf-39c7256271ca'::uuid
    and exists (
      select 1 from public.contact_messages
      where contact_messages.id = contact_message_replies.message_id
    )
  );

create or replace function public.mark_contact_message_read(p_message_id uuid)
returns void
language plpgsql
security invoker
set search_path = ''
as $$
declare
  updated_rows integer;
begin
  update public.contact_messages
  set is_read = true
  where id = p_message_id
    and auth.uid() = '94cec00f-fcca-464a-81cf-39c7256271ca'::uuid;

  get diagnostics updated_rows = row_count;
  if updated_rows = 0 then
    raise exception 'Contact message not found or access denied.';
  end if;
end;
$$;

revoke all on function public.mark_contact_message_read(uuid) from public, anon;
grant execute on function public.mark_contact_message_read(uuid) to authenticated;

create table public.contact_submission_rate_limits (
  ip_hash text primary key,
  window_started_at timestamptz not null,
  submissions integer not null check (submissions > 0)
);

alter table public.contact_submission_rate_limits enable row level security;
revoke all on public.contact_submission_rate_limits from public, anon, authenticated;

create or replace function public.consume_contact_submission(p_ip_hash text)
returns boolean
language plpgsql
security definer
set search_path = ''
as $$
declare
  submission_count integer;
begin
  insert into public.contact_submission_rate_limits (ip_hash, window_started_at, submissions)
  values (p_ip_hash, now(), 1)
  on conflict (ip_hash) do update
  set
    submissions = case
      when public.contact_submission_rate_limits.window_started_at <= now() - interval '1 hour' then 1
      else public.contact_submission_rate_limits.submissions + 1
    end,
    window_started_at = case
      when public.contact_submission_rate_limits.window_started_at <= now() - interval '1 hour' then now()
      else public.contact_submission_rate_limits.window_started_at
    end
  returning submissions into submission_count;

  return submission_count <= 5;
end;
$$;

revoke all on function public.consume_contact_submission(text) from public, anon, authenticated;
grant execute on function public.consume_contact_submission(text) to service_role;
