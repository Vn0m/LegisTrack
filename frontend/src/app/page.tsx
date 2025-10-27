"use client";
import BillSearch from '../components/BillSearch';
import BillCard from '../components/BillCard';
import BillModal from '../components/BillModal';
import { useState } from 'react';

export default function Home() {
  const [results, setResults] = useState<any>(null);
  const [open, setOpen] = useState<string | null>(null);

  const items = (results?.result?.items || []).map((x: any) => x.result || x);

  return (
    <main className="text-white p-6 space-y-6 min-h-screen">
      <div className="text-center space-y-2">
        <h1 className="text-2xl font-bold">LegisTrack</h1>
        <p className="text-gray-400">Search NY State legislation, get AI summaries, and track bills.</p>
      </div>
      <div className="flex justify-center">
        <BillSearch onResults={setResults} />
      </div>
      <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
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
      <BillModal basePrintNoStr={open} onClose={() => setOpen(null)} />
    </main>
  );
}
