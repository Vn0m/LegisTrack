"use client";

import { useEffect, useState } from 'react';
import { getBill, summarizeBill } from '../lib/nysenate-api';

type Props = { basePrintNoStr: string | null; onClose: () => void };

export default function BillModal({ basePrintNoStr, onClose }: Props) {
  const [bill, setBill] = useState<any>(null);
  const [summary, setSummary] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setSummary(null);
    setBill(null);
    
    if (!basePrintNoStr) return;
    (async () => {
      setLoading(true);
      try {
        const data = await getBill(basePrintNoStr);
        setBill(data);
      } finally {
        setLoading(false);
      }
    })();
  }, [basePrintNoStr]);

  const doSummarize = async () => {
    if (!basePrintNoStr) return;
    setLoading(true);
    try {
      const res = await summarizeBill(basePrintNoStr);
      setSummary(typeof res === 'string' ? res : '');
    } catch (err: any) {
      console.error('Failed to summarize:', err);
      setSummary(`Error: ${err?.message || 'Failed to generate summary. The bill may not have enough content to summarize.'}`);
    } finally {
      setLoading(false);
    }
  };

  if (!basePrintNoStr) return null;

  const result = bill?.result;
  const sponsor = result?.sponsor?.member;
  const chamber = result?.billType?.chamber || result?.chamber;

  return (
    <div className="fixed inset-0 bg-black/70 flex items-center justify-center p-4 z-50">
      <div className="bg-gray-900 border border-gray-700 rounded-lg shadow-2xl max-w-4xl w-full max-h-[90vh] overflow-hidden flex flex-col">
        <div className="bg-gradient-to-r from-blue-600 to-blue-700 p-6 border-b border-gray-700">
          <div className="flex justify-between items-start">
            <div className="flex-1">
              <div className="flex items-center gap-3 mb-3">
                <span className="inline-block bg-white/20 text-white text-xs font-semibold px-3 py-1 rounded">
                  {basePrintNoStr}
                </span>
                {chamber && (
                  <span className={`text-xs px-2 py-1 rounded font-medium ${
                    chamber === 'SENATE' 
                      ? 'bg-green-500/20 text-green-300' 
                      : 'bg-purple-500/20 text-purple-300'
                  }`}>
                    {chamber === 'SENATE' ? 'Senate' : 'Assembly'}
                  </span>
                )}
              </div>
              <h2 className="text-2xl font-bold text-white">{result?.title || 'Loading...'}</h2>
            </div>
            <button 
              className="text-white/90 hover:text-white transition-colors ml-4"
              onClick={onClose}
            >
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>

        <div className="overflow-y-auto flex-1 p-6 bg-gray-950">
          {loading && !result && (
            <div className="flex items-center justify-center py-12">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
            </div>
          )}

          {result && (
            <div className="space-y-6">
              {result.summary && (
                <div className="text-gray-300">
                  <p className="leading-relaxed">{result.summary}</p>
                </div>
              )}

              <div className="space-y-3 text-sm">
                {result.session && (
                  <div className="flex items-center gap-3">
                    <svg className="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    <div>
                      <span className="text-gray-500">Session</span>
                      <p className="text-gray-300 font-medium">{result.session}</p>
                    </div>
                  </div>
                )}
                {result.status?.statusDesc && (
                  <div className="flex items-center gap-3">
                    <svg className="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2m2 2a2 2 0 00-2-2m0 0V3a2 2 0 012-2h2a2 2 0 012 2v2M7 9h6" />
                    </svg>
                    <div>
                      <span className="text-gray-500">Status</span>
                      <p className="text-gray-300 font-medium">{result.status.statusDesc}</p>
                    </div>
                  </div>
                )}
                {sponsor?.fullName && (
                  <div className="flex items-center gap-3">
                    <svg className="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                    <div>
                      <span className="text-gray-500">Sponsored by</span>
                      <p className="text-gray-300 font-medium">{sponsor.fullName}</p>
                      {sponsor.districtCode && (
                        <p className="text-gray-500 text-xs">District {sponsor.districtCode}</p>
                      )}
                    </div>
                  </div>
                )}
              </div>

              <div className="border-t border-gray-800 pt-6">
                {!summary ? (
                  <button 
                    onClick={doSummarize} 
                    className="w-full bg-gray-800 hover:bg-gray-700 text-white rounded-lg px-6 py-3 font-medium transition-colors flex items-center justify-center gap-2 disabled:opacity-50 border border-gray-700"
                    disabled={loading}
                  >
                    {loading ? (
                      <>
                        <svg className="animate-spin h-5 w-5" viewBox="0 0 24 24">
                          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                        </svg>
                        Summarizing...
                      </>
                    ) : (
                      <>
                        <svg className="w-5 h-5 text-yellow-400" fill="currentColor" viewBox="0 0 24 24">
                          <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
                        </svg>
                        Generate AI Summary
                      </>
                    )}
                  </button>
                ) : summary.startsWith('Error:') ? (
                  <div className="bg-gray-800 border border-gray-700 rounded-lg p-4">
                    <div className="flex items-center gap-2 mb-2">
                      <svg className="w-5 h-5 text-yellow-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                      </svg>
                      <h3 className="font-semibold text-yellow-400">Unable to Generate Summary</h3>
                    </div>
                    <p className="text-gray-400 text-sm">{summary.replace('Error: ', '')}</p>
                  </div>
                ) : (
                  <div>
                    <div className="flex items-center gap-2 mb-3">
                      <svg className="w-5 h-5 text-purple-400" fill="currentColor" viewBox="0 0 24 24">
                        <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
                      </svg>
                      <h3 className="font-bold text-white">AI Summary</h3>
                    </div>
                    <p className="text-gray-400 leading-relaxed pl-7">{summary}</p>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}



