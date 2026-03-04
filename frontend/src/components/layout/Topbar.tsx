"use client";

import { usePathname, useRouter } from "next/navigation";
import { useAuthStore } from "@/lib/store/auth.store";

const pageTitleMap: Record<string, string> = {
  "/transactions": "Transacoes",
  "/categories": "Categorias",
  "/ledger": "Fatura",
};

export function Topbar() {
  const pathname = usePathname();
  const router = useRouter();
  const user = useAuthStore((state) => state.user);
  const title = pageTitleMap[pathname] ?? "Zenith";

  return (
    <header className="sticky top-0 z-30 flex h-14 items-center justify-between border-b border-[var(--border)] bg-[var(--bg-base)]/90 px-4 backdrop-blur">
      <h2 className="font-display text-2xl italic">{title}</h2>
      <div className="flex items-center gap-2">
        <button
          onClick={() => router.push("/transactions?new=1")}
          className="focusable rounded-md border border-[var(--border)] bg-[var(--accent)] px-3 py-1.5 text-sm font-medium text-black transition-all duration-150 hover:bg-[var(--accent-hover)]"
        >
          Nova transacao
        </button>
        <span className="rounded-full bg-white/5 px-3 py-1 text-xs text-[var(--text-secondary)]">
          {user?.displayName ?? "usuario"}
        </span>
      </div>
    </header>
  );
}
