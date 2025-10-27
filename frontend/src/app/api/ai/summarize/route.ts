import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

export async function POST(req: NextRequest) {
  const body = await req.text();
  const res = await fetch(`${BACKEND_URL}/api/ai/summarize`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body,
  });
  
  if (!res.ok) {
    const errorText = await res.text();
    return new NextResponse(errorText, { status: res.status });
  }
  
  const text = await res.text();
  return new NextResponse(text, { 
    status: res.status, 
    headers: { 'Content-Type': 'application/json' } 
  });
}



