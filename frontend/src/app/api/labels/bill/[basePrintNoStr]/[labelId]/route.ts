import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

type Params = { basePrintNoStr: string; labelId: string };

export async function POST(req: NextRequest, { params }: { params: Params }) {
  const auth = req.headers.get('Authorization') || '';
  const res = await fetch(
    `${BACKEND_URL}/api/labels/bill/${encodeURIComponent(params.basePrintNoStr)}/${params.labelId}`,
    { method: 'POST', headers: { 'Authorization': auth } }
  );
  const text = await res.text();
  return new NextResponse(text, { status: res.status, headers: { 'Content-Type': 'application/json' } });
}

export async function DELETE(req: NextRequest, { params }: { params: Params }) {
  const auth = req.headers.get('Authorization') || '';
  const res = await fetch(
    `${BACKEND_URL}/api/labels/bill/${encodeURIComponent(params.basePrintNoStr)}/${params.labelId}`,
    { method: 'DELETE', headers: { 'Authorization': auth } }
  );
  const text = await res.text();
  return new NextResponse(text, { status: res.status, headers: { 'Content-Type': 'application/json' } });
}
