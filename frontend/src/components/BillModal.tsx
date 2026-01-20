"use client";

import { useEffect, useState, useRef } from 'react';
import { getBill, summarizeBill, saveBill, unsaveBill, checkIfSaved, updateNotes } from '../lib/nysenate-api';
import { supabase } from '../lib/supabase';

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

  useEffect(() => {
    setSummary(null);
    setBill(null);
    setIsSaved(false);
    setNotes('');
    
    if (!basePrintNoStr) return;
    (async () => {
      setLoading(true);
      try {
        const [data, savedStatus] = await Promise.all([
          getBill(basePrintNoStr),
          checkIfSaved(basePrintNoStr)
        ]);
        setBill(data);
        setIsSaved(savedStatus.saved);
        setNotes(savedStatus.notes);
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
      console.error('Failed to save/unsave:', err);
      alert(err.message || 'Failed to save bill');
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

  if (!basePrintNoStr) return null;

  const result = bill?.result;
  const sponsor = result?.sponsor?.member;
  const chamber = result?.billType?.chamber || result?.chamber;
  const isSenate = chamber?.toUpperCase() === 'SENATE';
  
  const session = result?.session || result?.year;
  const statusText = result?.status?.statusDesc || result?.status;
  const sponsorName = sponsor?.fullName || result?.sponsorName;
  const districtCode = sponsor?.districtCode;

  return (
    <div className="fixed inset-0 bg-black/80 flex items-center justify-center p-4 z-50" onClick={onClose}>
      <div 
        className="bg-[var(--surface)] border border-[var(--border)] max-w-3xl w-full max-h-[90vh] overflow-hidden flex flex-col"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="px-8 py-6 border-b border-[var(--border)]">
          <div className="flex justify-between items-start">
            <div className="flex-1">
              <div className="flex items-center gap-4 mb-3">
                <span className="text-[var(--accent)] text-sm font-mono">
                  {basePrintNoStr}
                </span>
                {chamber && (
                  <span className={`text-xs uppercase tracking-wide ${
                    isSenate ? 'text-[var(--senate)]' : 'text-[var(--assembly)]'
                  }`}>
                    {isSenate ? 'Senate' : 'Assembly'}
                  </span>
                )}
              </div>
              <h2 className="font-serif text-2xl font-semibold text-[var(--text-primary)] leading-tight">
                {result?.title || 'Loading...'}
              </h2>
            </div>
            <button 
              className="text-[var(--text-muted)] hover:text-[var(--text-primary)] transition-colors ml-6 p-1 cursor-pointer"
              onClick={onClose}
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>

        <div className="overflow-y-auto flex-1 px-8 py-6">
          {loading && !result && (
            <div className="flex items-center justify-center py-12">
              <div className="w-5 h-5 border border-[var(--text-muted)] border-t-transparent rounded-full animate-spin"></div>
            </div>
          )}

          {result && (
            <div className="space-y-6">
              {result.summary && (
                <p className="text-[var(--text-secondary)] leading-relaxed">
                  {result.summary}
                </p>
              )}

              <dl className="grid grid-cols-2 gap-4 text-sm border-t border-b border-[var(--border-muted)] py-5">
                {session && (
                  <div>
                    <dt className="text-[var(--text-muted)] text-xs uppercase tracking-wide mb-1">Session</dt>
                    <dd className="text-[var(--text-primary)]">{session}</dd>
                  </div>
                )}
                {statusText && (
                  <div>
                    <dt className="text-[var(--text-muted)] text-xs uppercase tracking-wide mb-1">Status</dt>
                    <dd className="text-[var(--text-primary)]">{statusText}</dd>
                  </div>
                )}
                {sponsorName && (
                  <div>
                    <dt className="text-[var(--text-muted)] text-xs uppercase tracking-wide mb-1">Sponsor</dt>
                    <dd className="text-[var(--text-primary)]">
                      {sponsorName}
                      {districtCode && (
                        <span className="text-[var(--text-muted)]"> (District {districtCode})</span>
                      )}
                    </dd>
                  </div>
                )}
              </dl>

              <div className="flex gap-3">
                {!summary && (
                  <button 
                    onClick={doSummarize} 
                    className="flex-1 border border-[var(--border)] hover:border-[var(--accent)] text-[var(--text-secondary)] hover:text-[var(--accent)] px-5 py-2.5 text-sm transition-colors disabled:opacity-50 cursor-pointer"
                    disabled={loading}
                  >
                    {loading ? 'Generating...' : 'Generate AI Summary'}
                  </button>
                )}
                
                {user ? (
                  <button 
                    onClick={handleSave}
                    disabled={saving}
                    className={`px-5 py-2.5 text-sm transition-colors disabled:opacity-50 cursor-pointer ${
                      isSaved 
                        ? 'border border-[var(--assembly)] text-[var(--assembly)] hover:bg-[var(--assembly)] hover:text-white' 
                        : 'border border-[var(--accent)] text-[var(--accent)] hover:bg-[var(--accent)] hover:text-[var(--background)]'
                    }`}
                  >
                    {saving ? '...' : isSaved ? 'Unsave' : 'Save'}
                  </button>
                ) : (
                  <button 
                    className="px-5 py-2.5 text-sm border border-[var(--border)] text-[var(--text-muted)] cursor-not-allowed"
                    disabled
                    title="Sign in to save bills"
                  >
                    Save
                  </button>
                )}
              </div>

              {isSaved && user && (
                <div className="pt-2">
                  <div className="flex items-center justify-between mb-2">
                    <label className="text-xs uppercase tracking-wide text-[var(--text-muted)]">
                      Your Notes
                    </label>
                    {savingNotes && (
                      <span className="text-xs text-[var(--text-muted)]">Saving...</span>
                    )}
                  </div>
                  <textarea
                    value={notes}
                    onChange={(e) => setNotes(e.target.value)}
                    onBlur={handleNotesBlur}
                    placeholder="Add personal notes about this bill..."
                    className="w-full bg-transparent border border-[var(--border)] px-4 py-3 text-sm text-[var(--text-primary)] placeholder:text-[var(--text-muted)] focus:outline-none focus:border-[var(--accent)] transition-colors resize-none"
                    rows={3}
                  />
                </div>
              )}
              
              {summary && (
                summary.startsWith('Error:') ? (
                  <div className="border-l-2 border-[var(--accent-muted)] pl-4 py-2">
                    <p className="text-[var(--text-muted)] text-sm">{summary.replace('Error: ', '')}</p>
                  </div>
                ) : (
                  <div className="border-l-2 border-[var(--accent)] pl-4 py-2">
                    <p className="text-xs uppercase tracking-wide text-[var(--text-muted)] mb-2">AI Summary</p>
                    <p className="text-[var(--text-secondary)] leading-relaxed">{summary}</p>
                  </div>
                )
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
