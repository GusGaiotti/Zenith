"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { BrandWordmark } from "@/components/brand/BrandWordmark";
import { APP_NAV_ITEMS, NavigationIcon } from "@/components/layout/NavigationIcons";
import { ThemeToggle } from "@/components/theme/ThemeToggle";
import { useLogout } from "@/hooks/useAuth";
import { useAuthStore } from "@/lib/store/auth.store";
import { cn } from "@/lib/utils/cn";

interface SidebarProps {
  collapsed: boolean;
  onCollapsedChange: (value: boolean) => void;
}

export function Sidebar({ collapsed, onCollapsedChange }: SidebarProps) {
  const pathname = usePathname();
  const router = useRouter();
  const logoutMutation = useLogout();
  const user = useAuthStore((state) => state.user);
  const askAiLocked = user ? !user.aiAccessAllowed : false;

  return (
    <aside
      className={cn(
        "fixed left-4 top-4 z-30 hidden h-[calc(100vh-2rem)] flex-col rounded-2xl border border-[var(--border)] bg-[var(--bg-surface)]/92 p-3 shadow-[0_18px_40px_rgba(2,8,22,0.48)] backdrop-blur md:flex",
        collapsed ? "w-[96px]" : "w-[260px]",
      )}
    >
      <div className={cn("px-2 py-3", collapsed && "text-center")}>
        <BrandWordmark compact={collapsed} className={collapsed ? "justify-center" : undefined} labelClassName="text-2xl text-[var(--text-primary)]" />
      </div>

      <nav className="flex-1 space-y-2 px-1">
        {APP_NAV_ITEMS.map((item) => {
          const active = pathname.startsWith(item.href);
          const locked = item.href === "/ask-ai" && askAiLocked;

          if (locked) {
            return (
              <div
                key={item.href}
                title={collapsed ? `${item.label} bloqueado` : undefined}
                className={cn(
                  "flex rounded-xl border border-[var(--border)] py-3 text-sm text-[var(--text-muted)] opacity-78",
                  collapsed ? "items-center justify-center px-0" : "items-center gap-3 px-4",
                )}
              >
                <span className="grid h-8 w-8 shrink-0 place-items-center rounded-lg bg-[var(--bg-elevated)] text-[var(--text-muted)]">
                  <NavigationIcon name="lock" />
                </span>
                {!collapsed ? <span className="flex-1">Pergunte a IA</span> : null}
              </div>
            );
          }

          return (
            <Link
              key={item.href}
              href={item.href}
              title={collapsed ? item.label : undefined}
              className={cn(
                "focusable flex rounded-xl border py-3 text-sm transition-colors duration-150",
                collapsed ? "items-center justify-center px-0" : "items-center gap-3 px-4",
                active
                  ? "border-[var(--accent)] bg-[var(--accent-muted)] text-[var(--accent-hover)]"
                  : "border-[var(--border)] text-[var(--text-secondary)] hover:border-[var(--accent)] hover:bg-[var(--bg-elevated)] hover:text-[var(--text-primary)]",
              )}
            >
              <span className="grid h-8 w-8 shrink-0 place-items-center rounded-lg bg-[var(--bg-elevated)]">
                <NavigationIcon name={item.icon} />
              </span>
              {!collapsed ? <span>{item.label}</span> : null}
            </Link>
          );
        })}
      </nav>

      <div className={cn("space-y-2 px-2 pt-3", collapsed && "text-center")}>
        <div className={collapsed ? "flex justify-center" : undefined}>
          <ThemeToggle compact={collapsed} />
        </div>
        <div className="border-t border-[var(--border)] pt-3">
        {!collapsed ? (
          <p className="px-1 text-xs text-[var(--text-secondary)]">Logado como {user?.displayName ?? "-"}</p>
        ) : (
          <div className="text-[10px] uppercase tracking-[0.08em] text-[var(--text-muted)]">{user?.displayName?.slice(0, 1) ?? "Z"}</div>
        )}
        </div>
        <button
          title={collapsed ? "Expandir menu" : undefined}
          className={cn(
            "focusable w-full rounded-xl border border-[var(--border)] py-3 text-sm text-[var(--text-secondary)] transition-colors duration-150 hover:border-[var(--accent)] hover:bg-[var(--bg-elevated)] hover:text-[var(--text-primary)]",
            collapsed ? "px-0 text-center" : "px-4 text-left",
          )}
          onClick={() => onCollapsedChange(!collapsed)}
        >
          <span className={cn("flex items-center", collapsed ? "justify-center" : "gap-3")}>
            <NavigationIcon name={collapsed ? "expand" : "collapse"} />
            {!collapsed ? <span>Ocultar menu</span> : null}
          </span>
        </button>
        <button
          title={collapsed ? "Sair" : undefined}
          className={cn(
            "focusable danger-chip w-full rounded-xl py-3 text-sm transition-colors duration-150 hover:border-[var(--expense)] hover:bg-[color-mix(in_srgb,var(--expense)_14%,transparent)]",
            collapsed ? "px-0 text-center" : "px-4 text-left",
          )}
          onClick={() => {
            logoutMutation.mutate(undefined, {
              onSettled: () => router.replace("/login"),
            });
          }}
        >
          {collapsed ? (
            <span className="flex justify-center">
              <NavigationIcon name="collapse" className="h-4 w-4 rotate-180" />
            </span>
          ) : (
            "Sair"
          )}
        </button>
      </div>
    </aside>
  );
}
