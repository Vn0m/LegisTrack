import { supabase } from './supabase';

export type SearchParams = {
  q: string;
  year?: string;
  chamber?: string;
  status?: string;
  committee?: string;
};

export async function searchBills(params: SearchParams) {
  const url = new URL('/api/bills/search', window.location.origin);
  url.searchParams.set('q', params.q);
  if (params.year) url.searchParams.set('year', params.year);
  if (params.chamber) url.searchParams.set('chamber', params.chamber);
  if (params.status) url.searchParams.set('status', params.status);
  if (params.committee) url.searchParams.set('committee', params.committee);
  const res = await fetch(url.toString());
  if (!res.ok) throw new Error('Failed to search bills');
  return res.json();
}

export async function semanticSearchBills(q: string) {
  const url = new URL('/api/bills/semantic-search', window.location.origin);
  url.searchParams.set('q', q);
  const res = await fetch(url.toString());
  if (!res.ok) throw new Error('Semantic search failed');
  return res.json();
}

export async function getBill(basePrintNoStr: string) {
  const [billId, year] = basePrintNoStr.split('-');
  const res = await fetch(`/api/bills/${encodeURIComponent(year)}/${encodeURIComponent(billId)}`);
  if (!res.ok) throw new Error('Failed to fetch bill');
  return res.json();
}

export async function summarizeBill(basePrintNoStr: string) {
  const { data: { session } } = await supabase.auth.getSession();
  if (!session) throw new Error('Sign in to generate AI summaries');

  const res = await fetch('/api/ai/summarize', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${session.access_token}`,
    },
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

export async function saveBill(basePrintNoStr: string, notes?: string) {
  const { data: { session } } = await supabase.auth.getSession();
  
  if (!session) {
    throw new Error('Not authenticated');
  }

  const res = await fetch('/api/saved-bills/save', {
    method: 'POST',
    headers: { 
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${session.access_token}`
    },
    body: JSON.stringify({ basePrintNoStr, notes }),
  });
  
  const text = await res.text();
  
  if (!res.ok) {
    throw new Error(text || 'Failed to save bill');
  }
  
  return text;
}

export async function unsaveBill(basePrintNoStr: string) {
  const { data: { session } } = await supabase.auth.getSession();
  
  if (!session) {
    throw new Error('Not authenticated');
  }

  const res = await fetch('/api/saved-bills/unsave', {
    method: 'DELETE',
    headers: { 
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${session.access_token}`
    },
    body: JSON.stringify({ basePrintNoStr }),
  });
  
  const text = await res.text();
  
  if (!res.ok) {
    throw new Error(text || 'Failed to unsave bill');
  }
  
  return text;
}

export type SavedStatus = { saved: boolean; notes: string };

export async function checkIfSaved(basePrintNoStr: string): Promise<SavedStatus> {
  const { data: { session } } = await supabase.auth.getSession();
  if (!session) return { saved: false, notes: '' };

  const res = await fetch(`/api/saved-bills/check?basePrintNoStr=${encodeURIComponent(basePrintNoStr)}`, {
    headers: { 'Authorization': `Bearer ${session.access_token}` }
  });

  if (!res.ok) return { saved: false, notes: '' };
  const data = await res.json();
  return { saved: data.saved === true, notes: data.notes || '' };
}

export async function updateNotes(basePrintNoStr: string, notes: string) {
  const { data: { session } } = await supabase.auth.getSession();
  
  if (!session) {
    throw new Error('Not authenticated');
}

  const res = await fetch('/api/saved-bills/notes', {
    method: 'PATCH',
    headers: { 
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${session.access_token}`
    },
    body: JSON.stringify({ basePrintNoStr, notes }),
  });
  
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || 'Failed to update notes');
  }
  
  return res.json();
}

export async function getMyBills() {
  const { data: { session } } = await supabase.auth.getSession();
  if (!session) throw new Error('Not authenticated');

  const res = await fetch('/api/saved-bills/my-bills', {
    headers: { 'Authorization': `Bearer ${session.access_token}` },
  });
  if (!res.ok) throw new Error('Failed to fetch saved bills');
  return res.json();
}

export type LabelInfo = { id: string; label: string };

export async function getLabels(): Promise<{ labels: LabelInfo[] }> {
  const res = await fetch('/api/labels');
  if (!res.ok) throw new Error('Failed to fetch labels');
  return res.json();
}

export async function createLabel(label: string): Promise<LabelInfo> {
  const { data: { session } } = await supabase.auth.getSession();
  if (!session) throw new Error('Not authenticated');
  const res = await fetch('/api/labels', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${session.access_token}` },
    body: JSON.stringify({ label }),
  });
  if (!res.ok) throw new Error('Failed to create label');
  return res.json();
}

export async function getBillLabels(basePrintNoStr: string): Promise<{ labels: LabelInfo[] }> {
  const res = await fetch(`/api/labels/bill/${encodeURIComponent(basePrintNoStr)}`);
  if (!res.ok) return { labels: [] };
  return res.json();
}

export async function addLabelToBill(basePrintNoStr: string, labelId: string): Promise<void> {
  const { data: { session } } = await supabase.auth.getSession();
  if (!session) throw new Error('Not authenticated');
  const res = await fetch(`/api/labels/bill/${encodeURIComponent(basePrintNoStr)}/${labelId}`, {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${session.access_token}` },
  });
  if (!res.ok) throw new Error('Failed to add label');
}

export async function removeLabelFromBill(basePrintNoStr: string, labelId: string): Promise<void> {
  const { data: { session } } = await supabase.auth.getSession();
  if (!session) throw new Error('Not authenticated');
  const res = await fetch(`/api/labels/bill/${encodeURIComponent(basePrintNoStr)}/${labelId}`, {
    method: 'DELETE',
    headers: { 'Authorization': `Bearer ${session.access_token}` },
  });
  if (!res.ok) throw new Error('Failed to remove label');
}

export type NotificationItem = {
  id: string;
  basePrintNoStr: string;
  billTitle: string;
  oldStatus: string | null;
  newStatus: string;
  createdAt: string;
  processed: boolean;
};

export async function getNotifications(): Promise<{ notifications: NotificationItem[]; unreadCount: number }> {
  const { data: { session } } = await supabase.auth.getSession();
  if (!session) return { notifications: [], unreadCount: 0 };
  const res = await fetch('/api/notifications', {
    headers: { 'Authorization': `Bearer ${session.access_token}` },
  });
  if (!res.ok) return { notifications: [], unreadCount: 0 };
  return res.json();
}

export async function markNotificationRead(id: string): Promise<void> {
  const { data: { session } } = await supabase.auth.getSession();
  if (!session) return;
  await fetch(`/api/notifications/${id}`, {
    method: 'PUT',
    headers: { 'Authorization': `Bearer ${session.access_token}` },
  });
}

export async function markAllNotificationsRead(): Promise<void> {
  const { data: { session } } = await supabase.auth.getSession();
  if (!session) return;
  await fetch('/api/notifications/read-all', {
    method: 'PUT',
    headers: { 'Authorization': `Bearer ${session.access_token}` },
  });
}
