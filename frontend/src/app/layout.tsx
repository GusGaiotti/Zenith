import type { Metadata } from "next";
import { IBM_Plex_Mono, Manrope, Sora } from "next/font/google";
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

const body = Manrope({
  variable: "--font-body",
  subsets: ["latin"],
  weight: ["400", "500", "600", "700"],
});

const display = Sora({
  variable: "--font-display",
  subsets: ["latin"],
  weight: ["500", "600", "700"],
});

const mono = IBM_Plex_Mono({
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
  description: "Gestão financeira compartilhada para casais",
  robots: {
    index: true,
    follow: true,
  },
  openGraph: {
    title: "Zenith",
    description: "Gestão financeira compartilhada para casais",
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
    <html lang="pt-BR" suppressHydrationWarning>
      <head>
        <script
          dangerouslySetInnerHTML={{
            __html: `(() => {
              try {
                const stored = localStorage.getItem("zenith-theme");
                document.documentElement.dataset.theme = stored === "dark" ? "dark" : "light";
              } catch {
                document.documentElement.dataset.theme = "light";
              }
            })();`,
          }}
        />
      </head>
      <body className={`${body.variable} ${display.variable} ${mono.variable} antialiased`}>
        <QueryProvider>
          <AuthBootstrap />
          {children}
        </QueryProvider>
      </body>
    </html>
  );
}
