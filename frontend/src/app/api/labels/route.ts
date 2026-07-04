import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

export async function GET() {
  const res = await fetch(`${BACKEND_URL}/api/labels`);
  const text = await res.text();
  return new NextResponse(text, { status: res.status, headers: { 'Content-Type': 'application/json' } });
}

export async function POST(req: NextRequest) {
  const body = await req.text();
  const auth = req.headers.get('Authorization') || '';
  const res = await fetch(`${BACKEND_URL}/api/labels`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': auth },
    body,
  });
  const text = await res.text();
  return new NextResponse(text, { status: res.status, headers: { 'Content-Type': 'application/json' } });
}
