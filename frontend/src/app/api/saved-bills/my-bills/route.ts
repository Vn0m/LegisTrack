import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

export async function GET(request: NextRequest) {
  const authHeader = request.headers.get('Authorization');

  const res = await fetch(`${BACKEND_URL}/api/saved-bills/my-bills`, {
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
