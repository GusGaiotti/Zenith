"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { AskAiWorkspace } from "@/components/ai/AskAiWorkspace";
import { LoadingSpinner } from "@/components/shared/LoadingSpinner";
import { useAuthStore } from "@/lib/store/auth.store";

export default function AskAiPage() {
  const router = useRouter();
  const user = useAuthStore((state) => state.user);
  const hydrated = useAuthStore((state) => state.hydrated);
  const authResolved = useAuthStore((state) => state.authResolved);

  useEffect(() => {
    if (hydrated && authResolved && user && !user.aiAccessAllowed) {
      router.replace("/dashboard");
    }
  }, [authResolved, hydrated, router, user]);

  if (!hydrated || !authResolved) {
    return <LoadingSpinner label="Carregando acesso do assistente..." />;
  }

  if (user && !user.aiAccessAllowed) {
    return <LoadingSpinner label="Redirecionando para o dashboard..." />;
  }

  return <AskAiWorkspace />;
}
