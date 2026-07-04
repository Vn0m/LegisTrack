type Milestone = { statusType: string; statusDesc: string; actionDate?: string };

type Props = { milestones: Milestone[]; chamber?: string };

const SENATE_TRACK: [string, string][] = [
  ['IN_SENATE_COMM', 'Senate Committee'],
  ['SENATE_FLOOR', 'Senate Floor'],
  ['PASSED_SENATE', 'Passed Senate'],
  ['IN_ASSEMBLY_COMM', 'Assembly Committee'],
  ['ASSEMBLY_FLOOR', 'Assembly Floor'],
  ['PASSED_ASSEMBLY', 'Passed Assembly'],
  ['DELIVERED_TO_GOV', 'To Governor'],
  ['SIGNED_BY_GOV', 'Signed into Law'],
];

const ASSEMBLY_TRACK: [string, string][] = [
  ['IN_ASSEMBLY_COMM', 'Assembly Committee'],
  ['ASSEMBLY_FLOOR', 'Assembly Floor'],
  ['PASSED_ASSEMBLY', 'Passed Assembly'],
  ['IN_SENATE_COMM', 'Senate Committee'],
  ['SENATE_FLOOR', 'Senate Floor'],
  ['PASSED_SENATE', 'Passed Senate'],
  ['DELIVERED_TO_GOV', 'To Governor'],
  ['SIGNED_BY_GOV', 'Signed into Law'],
];

const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

function shortDate(iso?: string) {
  if (!iso) return '';
  const [y, m, d] = iso.split('-');
  if (!y || !m || !d) return iso;
  return `${MONTHS[parseInt(m, 10) - 1]} ${parseInt(d, 10)} ’${y.slice(2)}`;
}

export default function BillJourney({ milestones, chamber }: Props) {
  if (!milestones || milestones.length === 0) return null;

  const vetoed = milestones.find(m => m.statusType === 'VETOED');
  const isAssembly = chamber?.toUpperCase() === 'ASSEMBLY';
  const track = (isAssembly ? ASSEMBLY_TRACK : SENATE_TRACK).map(([type, label]) => {
    if (type === 'SIGNED_BY_GOV' && vetoed) return ['VETOED', 'Vetoed'] as [string, string];
    return [type, label] as [string, string];
  });

  const reached = new Map(milestones.map(m => [m.statusType, m]));
  let currentIdx = -1;
  track.forEach(([type], i) => {
    if (reached.has(type)) currentIdx = i;
  });
  if (currentIdx === -1) return null;

  const n = track.length;
  const frac = currentIdx / (n - 1);

  return (
    <div className="overflow-x-auto pt-1 pb-1 -mx-1 px-1 outline-none">
      <div className="relative min-w-[600px]">
        <div
          className="absolute top-[9px] h-[2px] bg-[var(--border-muted)]"
          style={{ left: `${100 / (n * 2)}%`, right: `${100 / (n * 2)}%` }}
        />
        <div
          className="absolute top-[9px] h-[2px] bg-[var(--ink)] anim-draw"
          style={{ left: `${100 / (n * 2)}%`, width: `calc(${100 - 100 / n}% * ${frac})` }}
        />
        <div className="relative grid" style={{ gridTemplateColumns: `repeat(${n}, 1fr)` }}>
          {track.map(([type, label], i) => {
            const stop = reached.get(type);
            const isCurrent = i === currentIdx;
            const isVetoStop = type === 'VETOED' && !!stop;
            const isSignedStop = type === 'SIGNED_BY_GOV' && !!stop;
            const dotColor = isVetoStop
              ? 'var(--vetoed)'
              : isSignedStop
                ? 'var(--signed)'
                : isCurrent
                  ? 'var(--gold)'
                  : 'var(--ink)';
            return (
              <div key={type} className="flex flex-col items-center text-center px-1">
                <div className="h-5 flex items-center justify-center">
                  {stop ? (
                    <span
                      className={`anim-pop rounded-full ${isCurrent ? 'w-5 h-5 border-2 border-[var(--ink)]' : 'w-[14px] h-[14px]'}`}
                      style={{ background: dotColor, animationDelay: `${i * 60}ms` }}
                    />
                  ) : (
                    <span className="w-[14px] h-[14px] rounded-full bg-[var(--surface)] border-2 border-[var(--border)]" />
                  )}
                </div>
                <span
                  className={`font-mono text-[10px] uppercase tracking-wide mt-2 leading-tight ${
                    stop ? 'text-[var(--text-primary)] font-medium' : 'text-[var(--text-muted)]'
                  }`}
                >
                  {label}
                </span>
                {stop?.actionDate && (
                  <span className="font-mono text-[10px] text-[var(--text-muted)] mt-0.5">
                    {shortDate(stop.actionDate)}
                  </span>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
