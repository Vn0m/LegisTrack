import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

export async function PUT(req: NextRequest, { params }: { params: { id: string } }) {
  const auth = req.headers.get('Authorization') || '';
  const res = await fetch(`${BACKEND_URL}/api/notifications/${params.id}/read`, {
    method: 'PUT',
    headers: { 'Authorization': auth },
  });
  const text = await res.text();
  return new NextResponse(text, { status: res.status, headers: { 'Content-Type': 'application/json' } });
}
