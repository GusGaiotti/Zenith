"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { AppShell } from "@/components/layout/AppShell";
import { useAuthStore } from "@/lib/store/auth.store";

export default function ProtectedLayout({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const hydrated = useAuthStore((state) => state.hydrated);
  const authResolved = useAuthStore((state) => state.authResolved);
  const accessToken = useAuthStore((state) => state.accessToken);

  useEffect(() => {
    if (!hydrated || !authResolved) return;
    if (!accessToken) {
      router.replace("/login");
    }
  }, [accessToken, authResolved, hydrated, router]);

  if (!hydrated || !authResolved || !accessToken) {
    return (
      <main className="flex min-h-screen items-center justify-center">
        <p className="font-mono text-sm text-[var(--text-secondary)]">Carregando sessao...</p>
      </main>
    );
  }

  return <AppShell>{children}</AppShell>;
}
