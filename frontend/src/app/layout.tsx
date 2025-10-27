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
        <header className="w-full p-4 border-b flex items-center justify-between">
          <div className="font-semibold">LegisTrack</div>
          <AuthHeaderClient />
        </header>
        <div className="max-w-5xl mx-auto w-full">{children}</div>
      </body>
    </html>
  );
}
