"use client";

import { searchBills, semanticSearchBills } from '../lib/nysenate-api';
import { useState } from 'react';

type Props = { onResults: (data: any) => void };

export default function BillSearch({ onResults }: Props) {
  const [q, setQ] = useState('');
  const [year, setYear] = useState('');
  const [chamber, setChamber] = useState('');
  const [status, setStatus] = useState('');
  const [committee, setCommittee] = useState('');
  const [mode, setMode] = useState<'keyword' | 'semantic'>('keyword');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      let data: any;
      if (mode === 'semantic') {
        data = await semanticSearchBills(q);
      } else {
        data = await searchBills({
          q,
          year: year || undefined,
          chamber: chamber || undefined,
          status: status || undefined,
          committee: committee || undefined,
        });
      }
      onResults(data);
    } catch (err: any) {
      setError(err.message || 'Search failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="w-full max-w-2xl space-y-3">
      <div className="flex gap-1 mb-1">
        {(['keyword', 'semantic'] as const).map(m => (
          <button
            key={m}
            type="button"
            onClick={() => setMode(m)}
            className={`px-3 py-1 text-xs border transition-colors cursor-pointer ${
              mode === m
                ? 'border-[var(--accent)] text-[var(--accent)]'
                : 'border-[var(--border-muted)] text-[var(--text-muted)] hover:border-[var(--border)]'
            }`}
          >
            {m === 'keyword' ? 'Keyword' : 'Semantic'}
          </button>
        ))}
      </div>

      <form onSubmit={onSubmit} className="space-y-2">
        <div className="flex gap-3">
          <input
            className="flex-1 bg-transparent border border-[var(--border)] px-4 py-2.5 text-[var(--text-primary)] placeholder:text-[var(--text-muted)] focus:outline-none focus:border-[var(--accent)] transition-colors"
            placeholder={mode === 'semantic' ? 'Describe bills you\'re looking for…' : 'Search bills (e.g., housing, S2180)'}
            value={q}
            onChange={(e) => setQ(e.target.value)}
          />
          <button
            className="border border-[var(--accent)] text-[var(--accent)] hover:bg-[var(--accent)] hover:text-[var(--background)] px-6 py-2.5 text-sm font-medium transition-colors disabled:opacity-50 cursor-pointer"
            disabled={loading}
          >
            {loading ? '...' : 'Search'}
          </button>
        </div>

        {mode === 'keyword' && (
          <div className="flex gap-2">
            <input
              className="w-24 bg-transparent border border-[var(--border)] px-3 py-2 text-[var(--text-primary)] placeholder:text-[var(--text-muted)] focus:outline-none focus:border-[var(--accent)] transition-colors text-center text-sm"
              placeholder="Year"
              value={year}
              onChange={(e) => setYear(e.target.value)}
            />
            <select
              className="bg-transparent border border-[var(--border)] px-3 py-2 text-[var(--text-primary)] focus:outline-none focus:border-[var(--accent)] transition-colors text-sm cursor-pointer"
              value={chamber}
              onChange={(e) => setChamber(e.target.value)}
            >
              <option value="">All chambers</option>
              <option value="Senate">Senate</option>
              <option value="Assembly">Assembly</option>
            </select>
            <input
              className="flex-1 bg-transparent border border-[var(--border)] px-3 py-2 text-[var(--text-primary)] placeholder:text-[var(--text-muted)] focus:outline-none focus:border-[var(--accent)] transition-colors text-sm"
              placeholder="Status filter"
              value={status}
              onChange={(e) => setStatus(e.target.value)}
            />
            <input
              className="flex-1 bg-transparent border border-[var(--border)] px-3 py-2 text-[var(--text-primary)] placeholder:text-[var(--text-muted)] focus:outline-none focus:border-[var(--accent)] transition-colors text-sm"
              placeholder="Committee filter"
              value={committee}
              onChange={(e) => setCommittee(e.target.value)}
            />
          </div>
        )}

        {error && <span className="text-[var(--assembly)] text-sm">{error}</span>}
      </form>
    </div>
  );
}
