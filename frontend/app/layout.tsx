import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import Navbar from "@/components/Navbar";
import { Toaster } from "sonner";

const inter = Inter({ subsets: ["latin"], variable: "--font-sans" });

export const metadata: Metadata = {
  title: "Spendora — Personal Expense & Budget Tracking",
  description:
    "Track, organize, and understand your spending with real-time analytics and category budget controls.",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" className="dark">
      <body className={`${inter.variable} font-sans min-h-screen flex flex-col`}>
        <Navbar />
        <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8">
          {children}
        </main>
        <footer className="border-t border-white/5 py-6 text-center text-xs text-muted-foreground">
          <p>Spendora V1 • Personal Expense & Budget Tracking • INR (₹)</p>
        </footer>
        <Toaster position="top-right" richColors theme="dark" closeButton />
      </body>
    </html>
  );
}
