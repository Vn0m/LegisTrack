type Props = {
  title: string;
  basePrintNoStr: string;
  summary?: string;
  chamber?: string;
  score?: number;
  onOpen: (basePrintNoStr: string) => void;
};

export default function BillCard({ title, basePrintNoStr, summary, chamber, score, onOpen }: Props) {
  const isSenate = chamber?.toUpperCase() === 'SENATE';

  return (
    <article
      className="group border-b border-[var(--border-muted)] py-5 first:pt-0 cursor-pointer"
      onClick={() => onOpen(basePrintNoStr)}
    >
      <div className="flex items-baseline gap-3 mb-2">
        <span className="text-[var(--accent)] text-sm font-mono tracking-tight">
          {basePrintNoStr}
        </span>
        {chamber && (
          <span className={`text-xs uppercase tracking-wide ${
            isSenate ? 'text-[var(--senate)]' : 'text-[var(--assembly)]'
          }`}>
            {isSenate ? 'Senate' : 'Assembly'}
          </span>
        )}
        {typeof score === 'number' && (
          <span className="ml-auto text-xs text-[var(--text-muted)] border border-[var(--border-muted)] px-2 py-0.5">
            {Math.round(score * 100)}% match
          </span>
        )}
      </div>

      <h3 className="font-serif text-lg font-semibold text-[var(--text-primary)] leading-snug mb-2 group-hover:text-[var(--accent)] transition-colors">
        {title}
      </h3>

      {summary && (
        <p className="text-[var(--text-secondary)] text-sm leading-relaxed line-clamp-2">
          {summary}
        </p>
      )}
    </article>
  );
}
