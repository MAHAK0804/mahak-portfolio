import { createClient } from 'npm:@supabase/supabase-js@2.49.1';
import { corsHeaders, errorMessage, jsonResponse } from '../_shared/http.ts';

const ownerId = Deno.env.get('SUPABASE_ADMIN_USER_ID') ?? '';
const allowedOrigins = (Deno.env.get('ALLOWED_ORIGINS') ?? '')
  .split(',')
  .map((origin) => origin.trim())
  .filter(Boolean);

function escapeHtml(value: string): string {
  return value.replace(/[&<>"']/gu, (character) => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;',
  })[character]!);
}

Deno.serve(async (request) => {
  const origin = request.headers.get('Origin') ?? '';
  const headers = corsHeaders(origin, allowedOrigins);

  if (request.method === 'OPTIONS') {
    if (origin && allowedOrigins.length && !allowedOrigins.includes(origin)) {
      return jsonResponse({ error: 'This website is not allowed to send replies.' }, 403, headers);
    }
    return new Response('ok', { headers });
  }
  if (request.method !== 'POST') return jsonResponse({ error: 'Method not allowed.' }, 405, headers);

  try {
    const authorization = request.headers.get('Authorization');
    if (!authorization?.startsWith('Bearer ')) {
      return jsonResponse({ error: 'Owner sign-in is required.' }, 401, headers);
    }
    if (origin && allowedOrigins.length && !allowedOrigins.includes(origin)) {
      return jsonResponse({ error: 'This website is not allowed to send replies.' }, 403, headers);
    }

    const supabaseUrl = Deno.env.get('SUPABASE_URL')!;
    const serviceRoleKey = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')!;
    const userClient = createClient(supabaseUrl, Deno.env.get('SUPABASE_ANON_KEY')!, {
      global: { headers: { Authorization: authorization } },
      auth: { persistSession: false, autoRefreshToken: false },
    });
    const { data: { user }, error: authError } = await userClient.auth.getUser();
    if (authError || user?.id !== ownerId) {
      return jsonResponse({ error: 'Only the portfolio owner can send replies.' }, 403, headers);
    }

    const input = await request.json() as { replyId?: unknown };
    if (typeof input.replyId !== 'string' || !/^[0-9a-f-]{36}$/iu.test(input.replyId)) {
      return jsonResponse({ error: 'A valid reply ID is required.' }, 400, headers);
    }

    const emailApiKey = Deno.env.get('RESEND_API_KEY');
    const emailFrom = Deno.env.get('EMAIL_FROM');
    const ownerEmail = Deno.env.get('OWNER_EMAIL');
    if (!emailApiKey || !emailFrom || !ownerEmail) {
      throw new Error('Email sending is not configured. Set RESEND_API_KEY, EMAIL_FROM, and OWNER_EMAIL.');
    }

    const adminClient = createClient(supabaseUrl, serviceRoleKey, {
      auth: { persistSession: false, autoRefreshToken: false },
    });
    const { data: reply, error: replyError } = await adminClient
      .from('contact_message_replies')
      .select('id, body, email_sent_at, contact_messages(full_name, email, subject)')
      .eq('id', input.replyId)
      .single();
    if (replyError || !reply) throw new Error(`Could not load the reply: ${replyError?.message ?? 'Reply not found.'}`);
    if (reply.email_sent_at) return jsonResponse({ ok: true, alreadySent: true }, 200, headers);

    const message = Array.isArray(reply.contact_messages) ? reply.contact_messages[0] : reply.contact_messages;
    if (!message?.email) throw new Error('The original contact message no longer has a recipient email address.');

    const subject = message.subject ? `Re: ${message.subject}` : 'Re: Your portfolio message';
    const escapedBody = escapeHtml(reply.body).replace(/\r?\n/gu, '<br>');
    const response = await fetch('https://api.resend.com/emails', {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${emailApiKey}`,
        'Content-Type': 'application/json',
        'Idempotency-Key': `portfolio-reply-${reply.id}`,
      },
      body: JSON.stringify({
        from: emailFrom,
        to: [message.email],
        reply_to: ownerEmail,
        subject,
        text: reply.body,
        html: `<p>${escapedBody}</p><p>— Mahak</p>`,
      }),
    });
    if (!response.ok) {
      const detail = await response.text();
      throw new Error(`Email provider rejected the reply (${response.status}): ${detail.slice(0, 500)}`);
    }

    const { error: updateError } = await adminClient
      .from('contact_message_replies')
      .update({ email_sent_at: new Date().toISOString() })
      .eq('id', reply.id)
      .is('email_sent_at', null);
    if (updateError) throw new Error(`The email was sent but its delivery status could not be saved: ${updateError.message}`);

    return jsonResponse({ ok: true }, 200, headers);
  } catch (error) {
    console.error(`Contact reply failed: ${errorMessage(error)}`);
    return jsonResponse({ error: errorMessage(error) }, 500, headers);
  }
});
