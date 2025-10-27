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
    <form onSubmit={onSubmit} className="flex gap-2 w-full max-w-2xl">
      <input
        className="flex-1 border rounded px-3 py-2"
        placeholder="Search bills (e.g., housing, S2180)"
        value={q}
        onChange={(e) => setQ(e.target.value)}
      />
      <input
        className="w-28 border rounded px-3 py-2"
        placeholder="Year"
        value={year}
        onChange={(e) => setYear(e.target.value)}
      />
      <button className="bg-blue-600 text-white rounded px-4 py-2" disabled={loading}>
        {loading ? 'Searching...' : 'Search'}
      </button>
      {error && <span className="text-red-600 text-sm">{error}</span>}
    </form>
  );
}



