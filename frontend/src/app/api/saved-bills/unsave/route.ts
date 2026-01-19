import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

export async function DELETE(request: NextRequest) {
  const authHeader = request.headers.get('Authorization');
  const body = await request.json();

  const res = await fetch(`${BACKEND_URL}/api/saved-bills/unsave`, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
      ...(authHeader && { 'Authorization': authHeader }),
    },
    body: JSON.stringify(body),
  });

  const text = await res.text();
  return new NextResponse(text, {
    status: res.status,
    headers: { 'Content-Type': 'application/json' },
  });
}
