import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import AuthHeaderClient from './AuthHeaderClient';

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

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
      <body className={`${geistSans.variable} ${geistMono.variable} antialiased`}>
        <header className="border-b border-[var(--border-muted)]">
          <div className="max-w-6xl mx-auto px-6 py-4 flex items-center justify-between">
            <div className="flex items-center gap-3">
              <span className="font-serif text-xl font-semibold tracking-tight">LegisTrack</span>
              <span className="text-[var(--text-muted)] text-xs uppercase tracking-widest hidden sm:inline">NY Legislature</span>
            </div>
            <AuthHeaderClient />
          </div>
        </header>
        <div className="max-w-6xl mx-auto w-full">{children}</div>
      </body>
    </html>
  );
}
