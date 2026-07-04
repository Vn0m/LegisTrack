import type { Metadata } from "next";
import "./globals.css";
import AuthHeaderClient from './AuthHeaderClient';
import NotificationBell from '../components/NotificationBell';

export const metadata: Metadata = {
  title: "LegisTrack",
  description: "Track New York State legislation from introduction to law",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className="antialiased">
        <header className="bg-[var(--ink)] text-white">
          <div className="max-w-6xl mx-auto px-6 py-4 flex items-center justify-between">
            <div className="flex items-baseline gap-3 min-w-0">
              <span className="font-display text-2xl font-black tracking-tight">LegisTrack</span>
              <span className="font-mono text-[10px] uppercase tracking-[0.25em] text-white/50 hidden sm:inline truncate">
                New York State Legislature
              </span>
            </div>
            <div className="flex items-center gap-2 shrink-0">
              <NotificationBell />
              <AuthHeaderClient />
            </div>
          </div>
          <div className="h-[3px] bg-[var(--gold)]" />
        </header>
        <div className="max-w-6xl mx-auto w-full">{children}</div>
      </body>
    </html>
  );
}
