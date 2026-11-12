export function corsHeaders(origin: string, allowedOrigins: string[]): HeadersInit {
  const allowAllOrigins = allowedOrigins.includes('*');
  return {
    'Access-Control-Allow-Headers': 'authorization, apikey, content-type, x-client-info',
    'Access-Control-Allow-Methods': 'POST, OPTIONS',
    'Access-Control-Max-Age': '86400',
    Vary: 'Origin',
    ...(allowAllOrigins
      ? { 'Access-Control-Allow-Origin': '*' }
      : allowedOrigins.includes(origin)
        ? { 'Access-Control-Allow-Origin': origin }
        : {}),
  };
}

export function jsonResponse(
  body: Record<string, unknown>,
  status: number,
  headers: HeadersInit,
): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...headers, 'Content-Type': 'application/json' },
  });
}

export function errorMessage(error: unknown): string {
  return error instanceof Error ? error.message : 'Unexpected server error';
}

export function encodeBase64Url(value: Uint8Array | string): string {
  const bytes = typeof value === 'string' ? new TextEncoder().encode(value) : value;
  let binary = '';
  for (const byte of bytes) binary += String.fromCharCode(byte);
  return btoa(binary).replaceAll('+', '-').replaceAll('/', '_').replace(/=+$/u, '');
}
