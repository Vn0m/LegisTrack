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

  const heading = (
    <h3 className="font-mono text-[11px] uppercase tracking-[0.2em] text-[var(--text-muted)] border-b-2 border-[var(--ink)] pb-2 mb-4">
      Tracked bills{bills.length > 0 ? ` — ${bills.length}` : ''}
    </h3>
  );

  if (!user) {
    return (
      <aside>
        {heading}
        <p className="text-sm text-[var(--text-secondary)]">Sign in to track bills and get status updates.</p>
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
    <aside>
      {heading}

      {loading && (
        <div className="flex items-center justify-center py-4">
          <div className="w-4 h-4 border-2 border-[var(--border)] border-t-[var(--ink)] rounded-full animate-spin"></div>
        </div>
      )}

      {!loading && allLabels.length > 0 && (
        <div className="flex flex-wrap gap-1.5 mb-4">
          <button
            onClick={() => setActiveLabel(null)}
            className={`px-2 py-0.5 font-mono text-[11px] cursor-pointer transition-colors ${
              activeLabel === null
                ? 'bg-[var(--ink)] text-white'
                : 'bg-[var(--surface)] border border-[var(--border)] text-[var(--text-secondary)] hover:border-[var(--ink)]'
            }`}
          >
            All
          </button>
          {allLabels.map(l => (
            <button
              key={l.id}
              onClick={() => setActiveLabel(prev => prev === l.id ? null : l.id)}
              className={`px-2 py-0.5 font-mono text-[11px] cursor-pointer transition-colors ${
                activeLabel === l.id
                  ? 'bg-[var(--ink)] text-white'
                  : 'bg-[var(--surface)] border border-[var(--border)] text-[var(--text-secondary)] hover:border-[var(--ink)]'
              }`}
            >
              {l.label}
            </button>
          ))}
        </div>
      )}

      {!loading && displayedBills.length === 0 && (
        <p className="text-sm text-[var(--text-secondary)]">
          {bills.length === 0
            ? 'Nothing tracked yet. Open a bill and press Track.'
            : 'No tracked bills with this label.'}
        </p>
      )}

      {!loading && displayedBills.length > 0 && (
        <div className="space-y-4">
          {displayedBills.map((bill) => {
            const isSenate = bill.basePrintNoStr.toUpperCase().startsWith('S');
            return (
              <button
                key={bill.id}
                onClick={() => onOpenBill(bill.basePrintNoStr)}
                className="w-full text-left group cursor-pointer flex gap-2.5 items-start"
              >
                <span
                  className="bullet w-5 h-5 text-[10px] mt-0.5"
                  style={{ background: isSenate ? 'var(--senate)' : 'var(--assembly)' }}
                >
                  {isSenate ? 'S' : 'A'}
                </span>
                <span className="min-w-0">
                  <span className="font-mono text-[11px] text-[var(--text-muted)] block">{bill.basePrintNoStr}</span>
                  <span className="text-sm font-medium text-[var(--text-primary)] group-hover:underline decoration-2 underline-offset-2 line-clamp-2 leading-snug">
                    {bill.title}
                  </span>
                </span>
              </button>
            );
          })}
        </div>
      )}
    </aside>
  );
}
