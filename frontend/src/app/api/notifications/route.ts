import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

export async function GET(req: NextRequest) {
  const auth = req.headers.get('Authorization') || '';
  const res = await fetch(`${BACKEND_URL}/api/notifications`, {
    headers: { 'Authorization': auth },
  });
  const text = await res.text();
  return new NextResponse(text, { status: res.status, headers: { 'Content-Type': 'application/json' } });
}
