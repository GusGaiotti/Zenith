import type { Metadata } from "next";
import { DM_Mono, Geist, Instrument_Serif } from "next/font/google";
import { AuthBootstrap } from "@/components/providers/AuthBootstrap";
import { QueryProvider } from "@/components/providers/QueryProvider";
import "./globals.css";

export const dynamic = "force-dynamic";

function resolveMetadataBase() {
  const raw =
    process.env.SITE_URL ??
    (process.env.VERCEL_PROJECT_PRODUCTION_URL
      ? `https://${process.env.VERCEL_PROJECT_PRODUCTION_URL}`
      : null) ??
    (process.env.VERCEL_URL ? `https://${process.env.VERCEL_URL}` : null) ??
    "http://localhost:3000";

  try {
    return new URL(raw);
  } catch {
    return new URL("http://localhost:3000");
  }
}

const display = Instrument_Serif({
  variable: "--font-display",
  subsets: ["latin"],
  weight: "400",
});

const body = Geist({
  variable: "--font-body",
  subsets: ["latin"],
});

const mono = DM_Mono({
  variable: "--font-mono",
  subsets: ["latin"],
  weight: ["400", "500"],
});

export const metadata: Metadata = {
  metadataBase: resolveMetadataBase(),
  title: {
    default: "Zenith",
    template: "%s | Zenith",
  },
  description: "Gestao financeira compartilhada para casais",
  robots: {
    index: true,
    follow: true,
  },
  openGraph: {
    title: "Zenith",
    description: "Gestao financeira compartilhada para casais",
    type: "website",
    locale: "pt_BR",
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="pt-BR">
      <body className={`${display.variable} ${body.variable} ${mono.variable} antialiased`}>
        <QueryProvider>
          <AuthBootstrap />
          {children}
        </QueryProvider>
      </body>
    </html>
  );
}
