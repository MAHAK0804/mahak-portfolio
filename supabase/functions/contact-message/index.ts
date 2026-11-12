import { createClient } from 'npm:@supabase/supabase-js@2.49.1';
import { corsHeaders, errorMessage, jsonResponse } from '../_shared/http.ts';

const ownerId = Deno.env.get('SUPABASE_ADMIN_USER_ID') ?? '';
const allowedOrigins = (Deno.env.get('ALLOWED_ORIGINS') ?? '')
  .split(',')
  .map((origin) => origin.trim())
  .filter(Boolean);

Deno.serve(async (request) => {
  const origin = request.headers.get('Origin') ?? '';
  const headers = corsHeaders(origin, allowedOrigins);

  if (request.method === 'OPTIONS') {
    if (!origin || !allowedOrigins.includes(origin)) {
      return jsonResponse({ error: 'This website is not allowed to submit contact messages.' }, 403, headers);
    }
    return new Response('ok', { headers });
  }
  if (request.method !== 'POST') {
    return jsonResponse({ error: 'Method not allowed.' }, 405, headers);
  }
  if (!origin || !allowedOrigins.includes(origin)) {
    return jsonResponse({ error: 'This website is not allowed to submit contact messages.' }, 403, headers);
  }

  try {
    if (!ownerId || !Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')) {
      throw new Error('Contact inbox server configuration is incomplete.');
    }

    const input = await request.json() as Record<string, unknown>;
    if (typeof input.website === 'string' && input.website.trim()) {
      return jsonResponse({ ok: true }, 200, headers);
    }

    const fullName = typeof input.fullName === 'string' ? input.fullName.trim() : '';
    const email = typeof input.email === 'string' ? input.email.trim() : '';
    const subject = typeof input.subject === 'string' ? input.subject.trim() : '';
    const message = typeof input.message === 'string' ? input.message.trim() : '';

    if (
      fullName.length < 1 || fullName.length > 120
      || email.length < 3 || email.length > 320
      || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/u.test(email)
      || subject.length > 200
      || message.length < 1 || message.length > 5000
    ) {
      return jsonResponse({ error: 'Enter a valid name, email, and message.' }, 400, headers);
    }

    const ipAddress = request.headers.get('cf-connecting-ip')
      ?? request.headers.get('x-real-ip')
      ?? request.headers.get('x-forwarded-for')?.split(',')[0]?.trim();
    const rateLimitSalt = Deno.env.get('CONTACT_RATE_LIMIT_SALT');
    if (!ipAddress || !rateLimitSalt) {
      throw new Error('Contact message rate limiting is not configured.');
    }

    const ipHashBytes = await crypto.subtle.digest(
      'SHA-256',
      new TextEncoder().encode(`${rateLimitSalt}:${ipAddress}`),
    );
    const ipHash = Array.from(new Uint8Array(ipHashBytes), (byte) => byte.toString(16).padStart(2, '0')).join('');
    const supabase = createClient(
      Deno.env.get('SUPABASE_URL')!,
      Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')!,
      { auth: { persistSession: false, autoRefreshToken: false } },
    );
    const { data: allowed, error: rateLimitError } = await supabase.rpc(
      'consume_contact_submission',
      { p_ip_hash: ipHash },
    );
    if (rateLimitError) throw new Error(`Contact message rate limit failed: ${rateLimitError.message}`);
    if (!allowed) return jsonResponse({ error: 'Too many messages. Please try again later.' }, 429, headers);

    const { error } = await supabase
      .from('contact_messages')
      .insert({ full_name: fullName, email, subject, message });
    if (error) throw new Error(`Could not save your message: ${error.message}`);

    return jsonResponse({ ok: true }, 201, headers);
  } catch (error) {
    console.error(`Contact message submission failed: ${errorMessage(error)}`);
    return jsonResponse({ error: 'Your message could not be sent. Please try again later.' }, 500, headers);
  }
});
