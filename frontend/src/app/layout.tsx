import type { Metadata } from "next";
import "./globals.css";
import AuthHeaderClient from './AuthHeaderClient';
import NotificationBell from '../components/NotificationBell';

export const metadata: Metadata = {
  title: "LegisTrack",
  description: "Search NY legislation with AI summaries",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className="antialiased">
        <header className="border-b border-[var(--border-muted)]">
          <div className="max-w-6xl mx-auto px-6 py-4 flex items-center justify-between">
            <div className="flex items-center gap-3">
              <span className="font-serif text-xl font-semibold tracking-tight">LegisTrack</span>
              <span className="text-[var(--text-muted)] text-xs uppercase tracking-widest hidden sm:inline">NY Legislature</span>
            </div>
            <div className="flex items-center gap-1">
              <NotificationBell />
              <AuthHeaderClient />
            </div>
          </div>
        </header>
        <div className="max-w-6xl mx-auto w-full">{children}</div>
      </body>
    </html>
  );
}
