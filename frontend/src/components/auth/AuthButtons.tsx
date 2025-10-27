"use client";
import { useEffect, useState } from 'react';
import { supabase } from '../../lib/supabase';

export default function AuthButtons() {
  const [userEmail, setUserEmail] = useState<string | null>(null);
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

  const signIn = async () => {
    const email = prompt('Enter email for magic link sign-in');
    if (!email) return;
    await supabase.auth.signInWithOtp({ email });
    alert('Check your email for the sign-in link.');
  };
  const signOut = async () => {
    await supabase.auth.signOut();
  };

  return (
    <div className="flex items-center gap-3">
      {userEmail ? (
        <>
          <span className="text-sm text-gray-700">{userEmail}</span>
          <button className="text-sm text-blue-600" onClick={signOut}>Sign out</button>
        </>
      ) : (
        <button className="text-sm text-blue-600" onClick={signIn}>Sign in</button>
      )}
    </div>
  );
}



