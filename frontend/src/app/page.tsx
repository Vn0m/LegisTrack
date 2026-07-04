"use client";
import BillSearch from '../components/BillSearch';
import BillCard from '../components/BillCard';
import BillModal from '../components/BillModal';
import Dashboard from '../components/Dashboard';
import { useEffect, useState } from 'react';

export default function Home() {
  const [results, setResults] = useState<any>(null);
  const [open, setOpen] = useState<string | null>(null);

  useEffect(() => {
    const handler = (e: Event) => setOpen((e as CustomEvent<string>).detail);
    window.addEventListener('legistrack:open-bill', handler);
    return () => window.removeEventListener('legistrack:open-bill', handler);
  }, []);

  const seen = new Set<string>();
  const items = [
    ...(results?.apiResults || []),
    ...(results?.localResults || []),
    ...(results?.results || []),
  ].filter((item: any) => {
    if (!item.basePrintNoStr || seen.has(item.basePrintNoStr)) return false;
    seen.add(item.basePrintNoStr);
    return true;
  });

  return (
    <main className="px-6 py-10">
      <div className="mb-8">
        <h1 className="font-display font-black text-3xl sm:text-4xl tracking-tight leading-none mb-2">
          Follow a bill from introduction to law.
        </h1>
        <p className="font-mono text-xs text-[var(--text-muted)]">
          Search by topic, sponsor, or print number — every bill, every step.
        </p>
      </div>

      <div className="mb-10">
        <BillSearch onResults={setResults} />
      </div>

      <div className="flex gap-10">
        <div className="flex-1 min-w-0">
          {items.length > 0 ? (
            <>
              <p className="font-mono text-[11px] uppercase tracking-[0.2em] text-[var(--text-muted)] border-b-2 border-[var(--ink)] pb-2 mb-1">
                Results — {items.length}
              </p>
              <div className="divide-y divide-[var(--border-muted)]">
                {items.map((it: any) => (
                  <BillCard
                    key={it.basePrintNoStr}
                    title={it.title}
                    basePrintNoStr={it.basePrintNoStr}
                    summary={it.summary}
                    chamber={it.chamber}
                    status={it.status}
                    score={it.score}
                    onOpen={setOpen}
                  />
                ))}
              </div>
            </>
          ) : (
            <div className="py-14">
              <p className="font-mono text-[11px] uppercase tracking-[0.2em] text-[var(--text-muted)] border-b-2 border-[var(--ink)] pb-2 mb-8">
                How a bill becomes law
              </p>
              <div className="overflow-x-auto pb-1">
                <div className="relative min-w-[600px] max-w-2xl">
                  <div className="absolute top-[6px] h-[2px] bg-[var(--border)]" style={{ left: '6.25%', right: '6.25%' }} />
                  <div className="relative grid grid-cols-8">
                    {['Senate Committee', 'Senate Floor', 'Passed Senate', 'Assembly Committee', 'Assembly Floor', 'Passed Assembly', 'To Governor', 'Signed into Law'].map(label => (
                      <div key={label} className="flex flex-col items-center text-center px-1">
                        <span className="w-[13px] h-[13px] rounded-full bg-[var(--background)] border-2 border-[var(--border)]" />
                        <span className="font-mono text-[10px] uppercase tracking-wide mt-2 leading-tight text-[var(--text-muted)]">
                          {label}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
              <p className="text-[var(--text-secondary)] mt-10">
                Search above to see where any bill stands — try “housing”, “school funding”, or a print
                number like <span className="font-mono text-sm">S2180</span>.
              </p>
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
