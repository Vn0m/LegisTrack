"use client";

import { searchBills } from '../lib/nysenate-api';
import { useState } from 'react';

type Props = { onResults: (data: any) => void };

export default function BillSearch({ onResults }: Props) {
  const [q, setQ] = useState('');
  const [year, setYear] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const data = await searchBills({ q, year: year || undefined });
      onResults(data);
    } catch (err: any) {
      setError(err.message || 'Search failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={onSubmit} className="flex gap-3 w-full max-w-2xl">
      <input
        className="flex-1 bg-transparent border border-[var(--border)] px-4 py-2.5 text-[var(--text-primary)] placeholder:text-[var(--text-muted)] focus:outline-none focus:border-[var(--accent)] transition-colors"
        placeholder="Search bills (e.g., housing, S2180)"
        value={q}
        onChange={(e) => setQ(e.target.value)}
      />
      <input
        className="w-24 bg-transparent border border-[var(--border)] px-3 py-2.5 text-[var(--text-primary)] placeholder:text-[var(--text-muted)] focus:outline-none focus:border-[var(--accent)] transition-colors text-center"
        placeholder="Year"
        value={year}
        onChange={(e) => setYear(e.target.value)}
      />
      <button 
        className="border border-[var(--accent)] text-[var(--accent)] hover:bg-[var(--accent)] hover:text-[var(--background)] px-6 py-2.5 text-sm font-medium transition-colors disabled:opacity-50 cursor-pointer"
        disabled={loading}
      >
        {loading ? '...' : 'Search'}
      </button>
      {error && <span className="text-[var(--assembly)] text-sm self-center">{error}</span>}
    </form>
  );
}
