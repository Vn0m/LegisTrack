"use client";
import { useEffect, useState } from 'react';
import { supabase } from '../../lib/supabase';

export default function AuthButtons() {
  const [userEmail, setUserEmail] = useState<string | null>(null);
  const [showAuthModal, setShowAuthModal] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isSignUp, setIsSignUp] = useState(false);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    const getSession = async () => {
      const { data } = await supabase.auth.getSession();
      setUserEmail(data.session?.user?.email ?? null);
    };
    getSession();
    const { data: listener } = supabase.auth.onAuthStateChange((_e, session) => {
      setUserEmail(session?.user?.email ?? null);
    });
    return () => listener.subscription.unsubscribe();
  }, []);

  const handleAuth = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');

    try {
      if (isSignUp) {
        const { error } = await supabase.auth.signUp({ email, password });
        if (error) throw error;
        setMessage('Check your email to confirm your account.');
      } else {
        const { error } = await supabase.auth.signInWithPassword({ email, password });
        if (error) throw error;
        setShowAuthModal(false);
        setEmail('');
        setPassword('');
      }
    } catch (error: any) {
      setMessage(error.message || 'Authentication failed');
    } finally {
      setLoading(false);
    }
  };

  const signOut = async () => {
    await supabase.auth.signOut();
  };

  return (
    <>
      <div className="flex items-center gap-4">
        {userEmail ? (
          <>
            <span className="text-sm text-[var(--text-muted)]">{userEmail}</span>
            <button 
              className="text-sm text-[var(--text-secondary)] hover:text-[var(--text-primary)] transition-colors cursor-pointer" 
              onClick={signOut}
            >
              Sign out
            </button>
          </>
        ) : (
          <button 
            className="text-sm text-[var(--text-secondary)] hover:text-[var(--accent)] transition-colors cursor-pointer" 
            onClick={() => setShowAuthModal(true)}
          >
            Sign in
          </button>
        )}
      </div>

      {showAuthModal && (
        <div 
          className="fixed inset-0 bg-black/80 flex items-center justify-center z-50" 
          onClick={() => setShowAuthModal(false)}
        >
          <div 
            className="bg-[var(--surface)] border border-[var(--border)] p-8 max-w-sm w-full mx-4" 
            onClick={(e) => e.stopPropagation()}
          >
            <div className="flex justify-between items-center mb-6">
              <h2 className="font-serif text-xl font-semibold text-[var(--text-primary)]">
                {isSignUp ? 'Create Account' : 'Sign In'}
              </h2>
              <button 
                onClick={() => setShowAuthModal(false)} 
                className="text-[var(--text-muted)] hover:text-[var(--text-primary)] transition-colors cursor-pointer"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <form onSubmit={handleAuth} className="space-y-4">
              <div>
                <label className="block text-xs uppercase tracking-wide text-[var(--text-muted)] mb-2">Email</label>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full px-4 py-2.5 bg-transparent border border-[var(--border)] text-[var(--text-primary)] focus:outline-none focus:border-[var(--accent)] transition-colors"
                  required
                />
              </div>

              <div>
                <label className="block text-xs uppercase tracking-wide text-[var(--text-muted)] mb-2">Password</label>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full px-4 py-2.5 bg-transparent border border-[var(--border)] text-[var(--text-primary)] focus:outline-none focus:border-[var(--accent)] transition-colors"
                  required
                  minLength={6}
                />
              </div>

              {message && (
                <p className={`text-sm ${message.includes('error') || message.includes('failed') ? 'text-[var(--assembly)]' : 'text-[var(--senate)]'}`}>
                  {message}
                </p>
              )}

              <button
                type="submit"
                disabled={loading}
                className="w-full border border-[var(--accent)] text-[var(--accent)] hover:bg-[var(--accent)] hover:text-[var(--background)] py-2.5 text-sm font-medium transition-colors disabled:opacity-50 cursor-pointer"
              >
                {loading ? '...' : (isSignUp ? 'Create Account' : 'Sign In')}
              </button>

              <div className="text-center pt-2">
                <button
                  type="button"
                  onClick={() => setIsSignUp(!isSignUp)}
                  className="text-sm text-[var(--text-muted)] hover:text-[var(--text-secondary)] transition-colors cursor-pointer"
                >
                  {isSignUp ? 'Already have an account? Sign in' : "Don't have an account? Sign up"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </>
  );
}
