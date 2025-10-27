export type SearchParams = { q: string; year?: string };

export async function searchBills(params: SearchParams) {
  const url = new URL('/api/bills/search', window.location.origin);
  url.searchParams.set('q', params.q);
  if (params.year) url.searchParams.set('year', params.year);
  const res = await fetch(url.toString());
  if (!res.ok) throw new Error('Failed to search bills');
  return res.json();
}

export async function getBill(basePrintNoStr: string) {
  const [billId, year] = basePrintNoStr.split('-');
  const res = await fetch(`/api/bills/${encodeURIComponent(year)}/${encodeURIComponent(billId)}`);
  if (!res.ok) throw new Error('Failed to fetch bill');
  return res.json();
}

export async function summarizeBill(basePrintNoStr: string) {
  const res = await fetch('/api/ai/summarize', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ basePrintNoStr }),
  });
  
  const text = await res.text();
  
  if (!res.ok) {
    throw new Error(text || 'Failed to summarize bill');
  }
  
  try {
    const data = JSON.parse(text);
    
    if (data.error) {
      throw new Error(data.error);
    }
    
    if (Array.isArray(data) && data[0]?.summary_text) {
      return data[0].summary_text;
    }
    
    return text;
  } catch (e) {
    throw new Error(text || 'Failed to parse response');
  }
}


