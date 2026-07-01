"use client";

import { useEffect, useState } from 'react';
import { getMyBills, LabelInfo } from '../lib/nysenate-api';
import { supabase } from '../lib/supabase';

type SavedBill = {
  id: string;
  basePrintNoStr: string;
  title: string;
  savedAt: string;
  notes: string;
  labels: LabelInfo[];
};

type Props = {
  onOpenBill: (basePrintNoStr: string) => void;
};

export default function Dashboard({ onOpenBill }: Props) {
  const [user, setUser] = useState<any>(null);
  const [bills, setBills] = useState<SavedBill[]>([]);
  const [loading, setLoading] = useState(false);
  const [activeLabel, setActiveLabel] = useState<string | null>(null);

  useEffect(() => {
    const getSession = async () => {
      const { data: { session } } = await supabase.auth.getSession();
      setUser(session?.user || null);
    };
    getSession();

    const { data: { subscription } } = supabase.auth.onAuthStateChange((_event, session) => {
      setUser(session?.user || null);
    });

    return () => subscription.unsubscribe();
  }, []);

  useEffect(() => {
    if (!user) {
      setBills([]);
      return;
    }

    const fetchBills = async () => {
      setLoading(true);
      try {
        const data = await getMyBills();
        setBills(data.bills || []);
      } catch (err) {
        console.error('Failed to fetch saved bills:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchBills();
  }, [user]);

  if (!user) {
    return (
      <aside className="border-l border-[var(--border-muted)] pl-6">
        <h3 className="text-xs uppercase tracking-wide text-[var(--text-muted)] mb-3">Saved Bills</h3>
        <p className="text-sm text-[var(--text-muted)]">Sign in to save and track bills.</p>
      </aside>
    );
  }

  const allLabels: LabelInfo[] = Array.from(
    new Map(bills.flatMap(b => b.labels || []).map(l => [l.id, l])).values()
  );
  const displayedBills = activeLabel
    ? bills.filter(b => b.labels?.some(l => l.id === activeLabel))
    : bills;

  return (
    <aside className="border-l border-[var(--border-muted)] pl-6">
      <h3 className="text-xs uppercase tracking-wide text-[var(--text-muted)] mb-4">Saved Bills</h3>

      {loading && (
        <div className="flex items-center justify-center py-4">
          <div className="w-4 h-4 border border-[var(--text-muted)] border-t-transparent rounded-full animate-spin"></div>
        </div>
      )}

      {!loading && allLabels.length > 0 && (
        <div className="flex flex-wrap gap-1.5 mb-4">
          <button
            onClick={() => setActiveLabel(null)}
            className={`px-2 py-0.5 text-xs border transition-colors cursor-pointer ${
              activeLabel === null
                ? 'border-[var(--accent)] text-[var(--accent)]'
                : 'border-[var(--border-muted)] text-[var(--text-muted)] hover:border-[var(--border)]'
            }`}
          >
            All
          </button>
          {allLabels.map(l => (
            <button
              key={l.id}
              onClick={() => setActiveLabel(prev => prev === l.id ? null : l.id)}
              className={`px-2 py-0.5 text-xs border transition-colors cursor-pointer ${
                activeLabel === l.id
                  ? 'border-[var(--accent)] text-[var(--accent)]'
                  : 'border-[var(--border-muted)] text-[var(--text-muted)] hover:border-[var(--border)]'
              }`}
            >
              {l.label}
            </button>
          ))}
        </div>
      )}

      {!loading && displayedBills.length === 0 && (
        <p className="text-sm text-[var(--text-muted)]">{bills.length === 0 ? 'No saved bills yet.' : 'No bills with this label.'}</p>
      )}

      {!loading && displayedBills.length > 0 && (
        <div className="space-y-3">
          {displayedBills.map((bill) => (
            <button
              key={bill.id}
              onClick={() => onOpenBill(bill.basePrintNoStr)}
              className="w-full text-left group cursor-pointer"
            >
              <span className="text-[var(--accent)] text-xs font-mono">{bill.basePrintNoStr}</span>
              <p className="text-sm text-[var(--text-secondary)] group-hover:text-[var(--text-primary)] transition-colors truncate">
                {bill.title}
              </p>
            </button>
          ))}
        </div>
      )}
    </aside>
  );
}
