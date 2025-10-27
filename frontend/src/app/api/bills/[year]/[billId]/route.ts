import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

export async function GET(_req: NextRequest, context: { params: Promise<{ year: string; billId: string }> }) {
  const { year, billId } = await context.params;
  const url = `${BACKEND_URL}/api/bills/${encodeURIComponent(year)}/${encodeURIComponent(billId)}`;
  const res = await fetch(url);
  const text = await res.text();
  return new NextResponse(text, { status: res.status, headers: { 'Content-Type': 'application/json' } });
}


