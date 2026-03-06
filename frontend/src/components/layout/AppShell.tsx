"use client";

import type { ReactNode } from "react";
import { useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { AskAiDrawer } from "@/components/ai/AskAiDrawer";
import { APP_NAV_ITEMS, NavigationIcon } from "@/components/layout/NavigationIcons";
import { Sidebar } from "@/components/layout/Sidebar";

export function AppShell({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [askAiOpen, setAskAiOpen] = useState(false);

  return (
    <div className="min-h-screen md:flex">
      <Sidebar collapsed={sidebarCollapsed} onCollapsedChange={setSidebarCollapsed} onAskAiOpen={() => setAskAiOpen(true)} />
      <div className={`w-full min-w-0 flex-1 transition-[margin] duration-300 ${sidebarCollapsed ? "md:ml-[116px]" : "md:ml-[280px]"}`}>
        <main className="mx-auto w-full max-w-[1280px] px-4 py-4 pb-24 md:px-6 md:py-6 md:pb-6">
          {children}
        </main>
      </div>
      <AskAiDrawer open={askAiOpen} onClose={() => setAskAiOpen(false)} />
      <nav className="fixed bottom-0 left-0 right-0 z-20 grid grid-cols-5 border-t border-[var(--border)] bg-[var(--bg-surface)]/98 px-2 py-2 backdrop-blur md:hidden">
        {APP_NAV_ITEMS.map((item) => (
          <Link
            key={item.href}
            className={`rounded-xl px-2 py-2 text-center transition-colors duration-150 ${
              pathname.startsWith(item.href)
                ? "bg-[var(--accent-muted)] text-[var(--accent-hover)]"
                : "text-[var(--text-secondary)] hover:text-[var(--text-primary)]"
            }`}
            href={item.href}
          >
            <span className="flex flex-col items-center gap-1 text-[11px]">
              <NavigationIcon className="h-4 w-4" name={item.icon} />
              <span>{item.label}</span>
            </span>
          </Link>
        ))}
        <button
          type="button"
          className="rounded-xl px-2 py-2 text-center text-[var(--text-secondary)] transition-colors duration-150 hover:text-[var(--text-primary)]"
          onClick={() => setAskAiOpen(true)}
        >
          <span className="flex flex-col items-center gap-1 text-[11px]">
            <NavigationIcon className="h-4 w-4" name="ask-ai" />
            <span>Perguntar</span>
          </span>
        </button>
      </nav>
    </div>
  );
}
