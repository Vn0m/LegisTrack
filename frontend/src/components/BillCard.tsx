type Props = {
  title: string;
  basePrintNoStr: string;
  summary?: string;
  chamber?: string;
  status?: string;
  score?: number;
  onOpen: (basePrintNoStr: string) => void;
};

export default function BillCard({ title, basePrintNoStr, summary, chamber, status, score, onOpen }: Props) {
  const isSenate = (chamber || basePrintNoStr).toUpperCase().startsWith('S');
  const statusColor = status?.toLowerCase().includes('signed')
    ? 'var(--signed)'
    : status?.toLowerCase().includes('vetoed')
      ? 'var(--vetoed)'
      : 'var(--text-muted)';

  return (
    <article
      className="group py-5 flex gap-4 cursor-pointer"
      onClick={() => onOpen(basePrintNoStr)}
    >
      <span
        className="bullet w-9 h-9 text-base mt-0.5"
        style={{ background: isSenate ? 'var(--senate)' : 'var(--assembly)' }}
        aria-label={isSenate ? 'Senate bill' : 'Assembly bill'}
      >
        {isSenate ? 'S' : 'A'}
      </span>

      <div className="flex-1 min-w-0">
        <div className="flex items-baseline gap-3 mb-1">
          <span className="font-mono text-xs font-medium text-[var(--text-primary)]">
            {basePrintNoStr}
          </span>
          {typeof score === 'number' && (
            <span className="font-mono text-[11px] font-semibold bg-[var(--gold)] text-[var(--ink)] px-1.5 py-0.5">
              {Math.round(score * 100)}% match
            </span>
          )}
        </div>

        <h3 className="font-display font-bold text-lg leading-snug mb-1.5 group-hover:underline decoration-2 underline-offset-4">
          {title}
        </h3>

        {summary && summary !== title && (
          <p className="text-[var(--text-secondary)] text-sm leading-relaxed line-clamp-2">
            {summary}
          </p>
        )}

        {status && (
          <p className="font-mono text-[11px] uppercase tracking-wide text-[var(--text-muted)] mt-2 flex items-center gap-1.5">
            <span className="w-2 h-2 rounded-full inline-block" style={{ background: statusColor }} />
            {status}
          </p>
        )}
      </div>
    </article>
  );
}
