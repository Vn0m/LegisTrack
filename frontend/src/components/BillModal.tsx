"use client";

import { useEffect, useState, useRef } from 'react';
import { getBill, summarizeBill, saveBill, unsaveBill, checkIfSaved, updateNotes, getBillLabels, getLabels, addLabelToBill, removeLabelFromBill, createLabel, LabelInfo } from '../lib/nysenate-api';
import { supabase } from '../lib/supabase';
import BillJourney from './BillJourney';

type Props = { basePrintNoStr: string | null; onClose: () => void };

export default function BillModal({ basePrintNoStr, onClose }: Props) {
  const [bill, setBill] = useState<any>(null);
  const [summary, setSummary] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [user, setUser] = useState<any>(null);
  const [isSaved, setIsSaved] = useState(false);
  const [saving, setSaving] = useState(false);
  const [notes, setNotes] = useState('');
  const [savingNotes, setSavingNotes] = useState(false);
  const notesRef = useRef(notes);
  notesRef.current = notes;
  const [billLabels, setBillLabels] = useState<LabelInfo[]>([]);
  const [allLabels, setAllLabels] = useState<LabelInfo[]>([]);
  const [showLabelPicker, setShowLabelPicker] = useState(false);
  const [newLabelText, setNewLabelText] = useState('');

  useEffect(() => {
    setSummary(null);
    setBill(null);
    setIsSaved(false);
    setNotes('');
    setBillLabels([]);
    setShowLabelPicker(false);
    setNewLabelText('');

    if (!basePrintNoStr) return;
    (async () => {
      setLoading(true);
      try {
        const [data, savedStatus, labelsRes, allLabelsRes] = await Promise.all([
          getBill(basePrintNoStr),
          checkIfSaved(basePrintNoStr),
          getBillLabels(basePrintNoStr),
          getLabels(),
        ]);
        setBill(data);
        setIsSaved(savedStatus.saved);
        setNotes(savedStatus.notes);
        setBillLabels(labelsRes.labels);
        setAllLabels(allLabelsRes.labels);
      } finally {
        setLoading(false);
      }
    })();
  }, [basePrintNoStr]);

  useEffect(() => {
    const getSession = async () => {
      const { data: { session } } = await supabase.auth.getSession();
      setUser(session?.user || null);
    };
    getSession();

    const { data: { subscription } } = supabase.auth.onAuthStateChange((_event, session) => {
      setUser(session?.user || null);
    });

    return () => subscription.unsubscribe();
  }, []);

  const doSummarize = async () => {
    if (!basePrintNoStr) return;
    setLoading(true);
    try {
      const res = await summarizeBill(basePrintNoStr);
      setSummary(typeof res === 'string' ? res : '');
    } catch (err: any) {
      console.error('Failed to summarize:', err);
      setSummary(`Error: ${err?.message || 'Failed to generate summary.'}`);
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    if (!basePrintNoStr || !user) return;
    setSaving(true);
    try {
      if (isSaved) {
        await unsaveBill(basePrintNoStr);
        setIsSaved(false);
        setNotes('');
      } else {
        await saveBill(basePrintNoStr);
        setIsSaved(true);
      }
    } catch (err: any) {
      console.error('Failed to track/untrack:', err);
      alert(err.message || 'Failed to track bill');
    } finally {
      setSaving(false);
    }
  };

  const handleNotesBlur = async () => {
    if (!basePrintNoStr || !isSaved) return;
    setSavingNotes(true);
    try {
      await updateNotes(basePrintNoStr, notesRef.current);
    } catch (err: any) {
      console.error('Failed to save notes:', err);
    } finally {
      setSavingNotes(false);
    }
  };

  const handleAddLabel = async (labelId: string) => {
    if (!basePrintNoStr) return;
    await addLabelToBill(basePrintNoStr, labelId);
    const added = allLabels.find(l => l.id === labelId);
    if (added) setBillLabels(prev => [...prev, added]);
  };

  const handleRemoveLabel = async (labelId: string) => {
    if (!basePrintNoStr) return;
    await removeLabelFromBill(basePrintNoStr, labelId);
    setBillLabels(prev => prev.filter(l => l.id !== labelId));
  };

  const handleCreateLabel = async () => {
    const text = newLabelText.trim();
    if (!text || !basePrintNoStr) return;
    const newLabel = await createLabel(text);
    setAllLabels(prev => [...prev, newLabel]);
    await addLabelToBill(basePrintNoStr, newLabel.id);
    setBillLabels(prev => [...prev, newLabel]);
    setNewLabelText('');
  };

  if (!basePrintNoStr) return null;

  const [billNo, billYear] = basePrintNoStr.split('-');
  const pdfUrl = `https://legislation.nysenate.gov/pdf/bills/${billYear}/${billNo}`;

  const result = bill?.result;
  const sponsor = result?.sponsor?.member;
  const chamber = result?.billType?.chamber || result?.chamber;
  const isSenate = chamber ? chamber.toUpperCase() === 'SENATE' : basePrintNoStr.toUpperCase().startsWith('S');

  const session = result?.session || result?.year;
  const statusText = result?.status?.statusDesc || result?.status;
  const sponsorName = sponsor?.fullName || result?.sponsorName;
  const districtCode = sponsor?.districtCode;
  const committeeName = result?.status?.committeeName;

  const amendment = result?.amendments?.items?.[result?.activeVersion ?? ''];
  const coSponsors: any[] = amendment?.coSponsors?.items || [];
  const sameAs = amendment?.sameAs?.items?.[0];
  const actions: any[] = (result?.actions?.items || []).slice().reverse();
  const votes: any[] = result?.votes?.items || [];
  const milestones: any[] = result?.milestones?.items || [];

  const openBill = (ref: string) => {
    window.dispatchEvent(new CustomEvent('legistrack:open-bill', { detail: ref }));
  };

  const sectionHead = (label: string) => (
    <p className="font-mono text-[11px] uppercase tracking-[0.2em] text-[var(--text-muted)] border-b border-[var(--border-muted)] pb-1.5 mb-3">
      {label}
    </p>
  );

  return (
    <div className="fixed inset-0 bg-black/60 flex items-center justify-center p-4 z-50" onClick={onClose}>
      <div
        className="bg-[var(--surface)] border-2 border-[var(--ink)] max-w-3xl w-full max-h-[90vh] overflow-hidden flex flex-col"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="px-8 py-6 border-b-2 border-[var(--ink)]">
          <div className="flex justify-between items-start gap-6">
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-3 mb-3">
                <span
                  className="bullet w-9 h-9 text-base"
                  style={{ background: isSenate ? 'var(--senate)' : 'var(--assembly)' }}
                >
                  {isSenate ? 'S' : 'A'}
                </span>
                <span className="font-mono text-sm font-medium">{basePrintNoStr}</span>
                <span className="font-mono text-[11px] uppercase tracking-[0.2em] text-[var(--text-muted)]">
                  {isSenate ? 'Senate' : 'Assembly'}{session ? ` · ${session}` : ''}
                </span>
              </div>
              <h2 className="font-display font-black text-2xl text-[var(--text-primary)] leading-tight tracking-tight">
                {result?.title || 'Loading…'}
              </h2>
            </div>
            <button
              className="text-[var(--text-muted)] hover:text-[var(--text-primary)] transition-colors p-1 cursor-pointer shrink-0"
              onClick={onClose}
              aria-label="Close"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>

        <div className="overflow-y-auto flex-1 px-8 py-6">
          {loading && !result && (
            <div className="flex items-center justify-center py-12">
              <div className="w-5 h-5 border-2 border-[var(--border)] border-t-[var(--ink)] rounded-full animate-spin"></div>
            </div>
          )}

          {result && (
            <div className="space-y-7">
              {milestones.length > 0 && (
                <div>
                  {sectionHead('Progress')}
                  <BillJourney milestones={milestones} chamber={chamber} />
                </div>
              )}

              {result.summary && (
                <p className="text-[var(--text-secondary)] leading-relaxed">
                  {result.summary}
                </p>
              )}

              <dl className="grid grid-cols-2 gap-x-4 gap-y-4 text-sm border-t border-b border-[var(--border-muted)] py-5">
                {statusText && (
                  <div>
                    <dt className="font-mono text-[10px] uppercase tracking-[0.15em] text-[var(--text-muted)] mb-1">Status</dt>
                    <dd className="text-[var(--text-primary)] font-medium">{statusText}</dd>
                  </div>
                )}
                {committeeName && (
                  <div>
                    <dt className="font-mono text-[10px] uppercase tracking-[0.15em] text-[var(--text-muted)] mb-1">Committee</dt>
                    <dd className="text-[var(--text-primary)] font-medium">{committeeName}</dd>
                  </div>
                )}
                {sponsorName && (
                  <div>
                    <dt className="font-mono text-[10px] uppercase tracking-[0.15em] text-[var(--text-muted)] mb-1">Sponsor</dt>
                    <dd className="text-[var(--text-primary)] font-medium">
                      {sponsorName}
                      {districtCode && (
                        <span className="text-[var(--text-muted)] font-normal"> · District {districtCode}</span>
                      )}
                    </dd>
                  </div>
                )}
                {sameAs && (
                  <div>
                    <dt className="font-mono text-[10px] uppercase tracking-[0.15em] text-[var(--text-muted)] mb-1">Same as</dt>
                    <dd>
                      <button
                        onClick={() => openBill(`${sameAs.basePrintNo}-${sameAs.session}`)}
                        className="font-mono text-sm text-[var(--accent)] hover:underline decoration-2 underline-offset-2 cursor-pointer"
                      >
                        {sameAs.basePrintNo}-{sameAs.session}
                      </button>
                    </dd>
                  </div>
                )}
                {coSponsors.length > 0 && (
                  <div className="col-span-2">
                    <dt className="font-mono text-[10px] uppercase tracking-[0.15em] text-[var(--text-muted)] mb-1">Co-sponsors</dt>
                    <dd className="text-[var(--text-secondary)]">
                      {coSponsors.map((c: any) => c.fullName).join(', ')}
                    </dd>
                  </div>
                )}
              </dl>

              <div className="flex flex-wrap gap-3">
                {user ? (
                  <button
                    onClick={handleSave}
                    disabled={saving}
                    className={`px-5 py-2.5 text-sm font-semibold transition-colors disabled:opacity-60 cursor-pointer ${
                      isSaved
                        ? 'bg-[var(--surface)] border-2 border-[var(--ink)] text-[var(--text-primary)] hover:border-[var(--vetoed)] hover:text-[var(--vetoed)]'
                        : 'bg-[var(--ink)] border-2 border-[var(--ink)] text-white hover:bg-[var(--accent)] hover:border-[var(--accent)]'
                    }`}
                  >
                    {saving ? '…' : isSaved ? 'Untrack' : 'Track bill'}
                  </button>
                ) : (
                  <button
                    className="px-5 py-2.5 text-sm font-semibold border-2 border-[var(--border)] text-[var(--text-muted)] cursor-not-allowed"
                    disabled
                    title="Sign in to track bills"
                  >
                    Track bill
                  </button>
                )}

                {!summary && (
                  user ? (
                    <button
                      onClick={doSummarize}
                      className="px-5 py-2.5 text-sm font-semibold border-2 border-[var(--ink)] text-[var(--text-primary)] hover:bg-[var(--gold)] transition-colors disabled:opacity-60 cursor-pointer"
                      disabled={loading}
                    >
                      {loading ? 'Summarizing…' : 'AI summary'}
                    </button>
                  ) : (
                    <button
                      className="px-5 py-2.5 text-sm font-semibold border-2 border-[var(--border)] text-[var(--text-muted)] cursor-not-allowed"
                      disabled
                      title="Sign in to generate AI summaries"
                    >
                      AI summary
                    </button>
                  )
                )}

                <a
                  href={pdfUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="px-5 py-2.5 text-sm font-semibold border-2 border-[var(--ink)] text-[var(--text-primary)] hover:bg-[var(--ink)] hover:text-white transition-colors"
                >
                  Full text (PDF)
                </a>
              </div>

              {summary && (
                summary.startsWith('Error:') ? (
                  <div className="border-l-4 border-[var(--border)] pl-4 py-1">
                    <p className="text-[var(--text-secondary)] text-sm">{summary.replace('Error: ', '')}</p>
                  </div>
                ) : (
                  <div className="border-l-4 border-[var(--gold)] pl-4 py-1">
                    <p className="font-mono text-[11px] uppercase tracking-[0.2em] text-[var(--text-muted)] mb-2">AI summary</p>
                    <p className="text-[var(--text-secondary)] leading-relaxed">{summary}</p>
                  </div>
                )
              )}

              {isSaved && user && (
                <div>
                  <div className="flex items-baseline justify-between border-b border-[var(--border-muted)] pb-1.5 mb-3">
                    <p className="font-mono text-[11px] uppercase tracking-[0.2em] text-[var(--text-muted)]">Your notes</p>
                    {savingNotes && (
                      <span className="font-mono text-[10px] text-[var(--text-muted)]">Saving…</span>
                    )}
                  </div>
                  <textarea
                    value={notes}
                    onChange={(e) => setNotes(e.target.value)}
                    onBlur={handleNotesBlur}
                    placeholder="Why are you tracking this bill?"
                    className="w-full bg-[var(--background)] border border-[var(--border)] px-4 py-3 text-sm text-[var(--text-primary)] placeholder:text-[var(--text-muted)] focus:outline-none focus:border-[var(--ink)] transition-colors resize-none"
                    rows={3}
                  />
                </div>
              )}

              <div>
                <div className="flex items-baseline justify-between border-b border-[var(--border-muted)] pb-1.5 mb-3">
                  <p className="font-mono text-[11px] uppercase tracking-[0.2em] text-[var(--text-muted)]">Labels</p>
                  {user && (
                    <button
                      onClick={() => setShowLabelPicker(prev => !prev)}
                      className="font-mono text-[11px] text-[var(--accent)] hover:underline cursor-pointer"
                    >
                      {showLabelPicker ? 'Done' : 'Edit'}
                    </button>
                  )}
                </div>
                <div className="flex flex-wrap gap-1.5">
                  {billLabels.map(l => (
                    <span key={l.id} className="flex items-center gap-1 px-2 py-0.5 bg-[var(--background)] border border-[var(--border)] font-mono text-[11px] text-[var(--text-secondary)]">
                      {l.label}
                      {user && showLabelPicker && (
                        <button onClick={() => handleRemoveLabel(l.id)} className="text-[var(--text-muted)] hover:text-[var(--vetoed)] cursor-pointer leading-none px-0.5 -mr-0.5">×</button>
                      )}
                    </span>
                  ))}
                  {billLabels.length === 0 && !showLabelPicker && (
                    <span className="text-sm text-[var(--text-muted)]">None yet</span>
                  )}
                </div>
                {user && showLabelPicker && (
                  <div className="mt-2 space-y-2">
                    <div className="flex flex-wrap gap-1.5">
                      {allLabels.filter(l => !billLabels.find(bl => bl.id === l.id)).map(l => (
                        <button
                          key={l.id}
                          onClick={() => handleAddLabel(l.id)}
                          className="px-2 py-0.5 border border-dashed border-[var(--border)] font-mono text-[11px] text-[var(--text-muted)] hover:border-[var(--ink)] hover:text-[var(--text-primary)] cursor-pointer transition-colors"
                        >
                          + {l.label}
                        </button>
                      ))}
                    </div>
                    <div className="flex gap-2">
                      <input
                        value={newLabelText}
                        onChange={e => setNewLabelText(e.target.value)}
                        onKeyDown={e => e.key === 'Enter' && handleCreateLabel()}
                        placeholder="New label…"
                        className="flex-1 bg-[var(--background)] border border-[var(--border)] px-2 py-1 font-mono text-xs text-[var(--text-primary)] placeholder:text-[var(--text-muted)] focus:outline-none focus:border-[var(--ink)]"
                      />
                      <button
                        onClick={handleCreateLabel}
                        className="px-3 py-1 text-xs font-semibold border border-[var(--ink)] text-[var(--text-primary)] hover:bg-[var(--ink)] hover:text-white transition-colors cursor-pointer"
                      >
                        Create
                      </button>
                    </div>
                  </div>
                )}
              </div>

              {votes.length > 0 && (
                <div>
                  {sectionHead('Votes')}
                  <div className="space-y-2">
                    {votes.map((v: any, i: number) => (
                      <div key={i} className="text-sm flex flex-wrap items-baseline gap-x-3">
                        <span className="font-mono text-xs text-[var(--text-muted)]">{v.voteDate}</span>
                        <span className="font-medium text-[var(--text-primary)]">
                          {v.voteType}{v.committee?.name ? ` — ${v.committee.name}` : ''}
                        </span>
                        <span className="font-mono text-xs text-[var(--text-secondary)]">
                          {Object.entries(v.memberVotes?.items || {})
                            .map(([kind, val]: [string, any]) => `${kind} ${val?.size ?? val?.items?.length ?? 0}`)
                            .join(' · ')}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {actions.length > 0 && (
                <div>
                  {sectionHead('Full history')}
                  <div className="max-h-48 overflow-y-auto space-y-1.5">
                    {actions.map((a: any, i: number) => (
                      <div key={i} className="text-sm flex gap-3 items-baseline">
                        <span className="font-mono text-[11px] text-[var(--text-muted)] shrink-0 w-20">{a.date}</span>
                        <span className="text-[var(--text-secondary)]">{a.text}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
