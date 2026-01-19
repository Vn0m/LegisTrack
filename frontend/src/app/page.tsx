"use client";
import BillSearch from '../components/BillSearch';
import BillCard from '../components/BillCard';
import BillModal from '../components/BillModal';
import Dashboard from '../components/Dashboard';
import { useState } from 'react';

export default function Home() {
  const [results, setResults] = useState<any>(null);
  const [open, setOpen] = useState<string | null>(null);

  const items = (results?.result?.items || []).map((x: any) => x.result || x);

  return (
    <main className="px-6 py-8">
      <p className="text-[var(--text-muted)] text-center text-sm mb-8">
        Search NY State legislation, get AI summaries, and track bills.
      </p>
      
      <div className="flex justify-center mb-10">
        <BillSearch onResults={setResults} />
      </div>

      <div className="flex gap-10">
        <div className="flex-1 min-w-0">
          {items.length > 0 ? (
            <div className="divide-y divide-[var(--border-muted)]">
              {items.map((it: any, idx: number) => {
                const title = it.title || it.summary || "Bill";
                const base = it.basePrintNoStr || (it.basePrintNo && it.session ? `${it.basePrintNo}-${it.session}` : "");
                const chamber = it.billType?.chamber || it.chamber;
                
                return (
                  <BillCard
                    key={idx}
                    title={title}
                    basePrintNoStr={base}
                    summary={it.summary}
                    chamber={chamber}
                    onOpen={setOpen}
                  />
                );
              })}
            </div>
          ) : (
            <div className="text-center py-16 text-[var(--text-muted)]">
              Search for bills to get started
            </div>
          )}
        </div>
        
        <div className="w-72 shrink-0 hidden lg:block">
          <Dashboard onOpenBill={setOpen} />
        </div>
      </div>

      <BillModal key={open || 'closed'} basePrintNoStr={open} onClose={() => setOpen(null)} />
    </main>
  );
}
