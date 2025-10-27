import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

export async function GET(req: NextRequest) {
  const { searchParams } = new URL(req.url);
  const q = searchParams.get('q') || '';
  const year = searchParams.get('year');
  const url = new URL('/api/bills/search', BACKEND_URL);
  url.searchParams.set('q', q);
  if (year) url.searchParams.set('year', year);

  const res = await fetch(url.toString());
  const text = await res.text();
  return new NextResponse(text, { status: res.status, headers: { 'Content-Type': 'application/json' } });
}



