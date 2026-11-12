drop table if exists public.admin_push_tokens;

do $$
begin
  if exists (
    select 1
    from pg_publication_tables
    where pubname = 'supabase_realtime'
      and schemaname = 'public'
      and tablename = 'contact_messages'
  ) then
    execute 'alter publication supabase_realtime drop table public.contact_messages';
  end if;
end;
$$;
