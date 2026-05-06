import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

export async function GET(_req: NextRequest, { params }: { params: { basePrintNoStr: string } }) {
  const res = await fetch(`${BACKEND_URL}/api/labels/bill/${encodeURIComponent(params.basePrintNoStr)}`);
  const text = await res.text();
  return new NextResponse(text, { status: res.status, headers: { 'Content-Type': 'application/json' } });
}
