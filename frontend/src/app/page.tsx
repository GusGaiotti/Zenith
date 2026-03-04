"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/lib/store/auth.store";

export default function Home() {
  const router = useRouter();
  const hydrated = useAuthStore((state) => state.hydrated);
  const authResolved = useAuthStore((state) => state.authResolved);
  const accessToken = useAuthStore((state) => state.accessToken);

  useEffect(() => {
    if (!hydrated || !authResolved) return;

    if (!accessToken) {
      router.replace("/login");
      return;
    }

    router.replace("/dashboard");
  }, [accessToken, authResolved, hydrated, router]);

  return (
    <main className="flex min-h-screen items-center justify-center">
      <p className="font-mono text-sm text-[var(--text-secondary)]">Carregando Zenith...</p>
    </main>
  );
}
