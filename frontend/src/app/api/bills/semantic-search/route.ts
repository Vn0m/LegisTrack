import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

export async function GET(req: NextRequest) {
  const { searchParams } = new URL(req.url);
  const q = searchParams.get('q') || '';
  const url = new URL('/api/bills/semantic-search', BACKEND_URL);
  url.searchParams.set('q', q);
  const res = await fetch(url.toString());
  const text = await res.text();
  return new NextResponse(text, { status: res.status, headers: { 'Content-Type': 'application/json' } });
}
