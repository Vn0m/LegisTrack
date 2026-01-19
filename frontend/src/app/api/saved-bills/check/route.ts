import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

export async function GET(request: NextRequest) {
  const authHeader = request.headers.get('Authorization');
  const { searchParams } = new URL(request.url);
  const basePrintNoStr = searchParams.get('basePrintNoStr');

  const res = await fetch(`${BACKEND_URL}/api/saved-bills/check?basePrintNoStr=${encodeURIComponent(basePrintNoStr || '')}`, {
    headers: {
      ...(authHeader && { 'Authorization': authHeader }),
    },
  });

  const text = await res.text();
  return new NextResponse(text, {
    status: res.status,
    headers: { 'Content-Type': 'application/json' },
  });
}
