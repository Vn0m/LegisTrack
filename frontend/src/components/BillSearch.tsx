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
    <div className="w-full max-w-3xl space-y-3">
      <div className="inline-flex border-2 border-[var(--ink)]">
        {(['keyword', 'semantic'] as const).map(m => (
          <button
            key={m}
            type="button"
            onClick={() => setMode(m)}
            className={`px-4 py-1.5 font-mono text-xs uppercase tracking-wide cursor-pointer transition-colors ${
              mode === m
                ? 'bg-[var(--ink)] text-white'
                : 'bg-[var(--surface)] text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
            }`}
          >
            {m === 'keyword' ? 'Keyword' : 'Semantic'}
          </button>
        ))}
      </div>

      <form onSubmit={onSubmit} className="space-y-2">
        <div className="flex">
          <input
            className="flex-1 min-w-0 bg-[var(--surface)] border-2 border-r-0 border-[var(--ink)] px-4 py-3 text-[var(--text-primary)] placeholder:text-[var(--text-muted)] focus:outline-none"
            placeholder={mode === 'semantic' ? 'Describe the bills you’re looking for…' : 'Search bills — topic, sponsor, or print number'}
            value={q}
            onChange={(e) => setQ(e.target.value)}
          />
          <button
            className="bg-[var(--ink)] text-white hover:bg-[var(--accent)] px-7 font-display font-bold text-sm uppercase tracking-wide transition-colors disabled:opacity-60 cursor-pointer"
            disabled={loading}
          >
            {loading ? '…' : 'Search'}
          </button>
        </div>

        {mode === 'keyword' && (
          <div className="flex flex-wrap gap-2">
            <input
              className="w-24 bg-[var(--surface)] border border-[var(--border)] px-3 py-2 font-mono text-xs text-[var(--text-primary)] placeholder:text-[var(--text-muted)] focus:outline-none focus:border-[var(--ink)] text-center"
              placeholder="Year"
              value={year}
              onChange={(e) => setYear(e.target.value)}
            />
            <select
              className="bg-[var(--surface)] border border-[var(--border)] px-3 py-2 font-mono text-xs text-[var(--text-primary)] focus:outline-none focus:border-[var(--ink)] cursor-pointer"
              value={chamber}
              onChange={(e) => setChamber(e.target.value)}
            >
              <option value="">All chambers</option>
              <option value="Senate">Senate</option>
              <option value="Assembly">Assembly</option>
            </select>
            <input
              className="flex-1 min-w-32 bg-[var(--surface)] border border-[var(--border)] px-3 py-2 font-mono text-xs text-[var(--text-primary)] placeholder:text-[var(--text-muted)] focus:outline-none focus:border-[var(--ink)]"
              placeholder="Status"
              value={status}
              onChange={(e) => setStatus(e.target.value)}
            />
            <input
              className="flex-1 min-w-32 bg-[var(--surface)] border border-[var(--border)] px-3 py-2 font-mono text-xs text-[var(--text-primary)] placeholder:text-[var(--text-muted)] focus:outline-none focus:border-[var(--ink)]"
              placeholder="Committee"
              value={committee}
              onChange={(e) => setCommittee(e.target.value)}
            />
          </div>
        )}

        {error && <p className="text-sm text-[var(--vetoed)]">{error}</p>}
      </form>
    </div>
  );
}
