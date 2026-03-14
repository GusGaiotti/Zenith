"use client";

import { useEffect, useState } from "react";
import { cn } from "@/lib/utils/cn";

type ThemeMode = "light" | "dark";

function applyTheme(theme: ThemeMode) {
  document.documentElement.dataset.theme = theme;
  localStorage.setItem("zenith-theme", theme);
}

function SunIcon() {
  return (
    <svg aria-hidden className="h-4 w-4" fill="none" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.8" viewBox="0 0 24 24">
      <circle cx="12" cy="12" r="4" />
      <path d="M12 2.5v2.2" />
      <path d="M12 19.3v2.2" />
      <path d="m4.93 4.93 1.55 1.55" />
      <path d="m17.52 17.52 1.55 1.55" />
      <path d="M2.5 12h2.2" />
      <path d="M19.3 12h2.2" />
      <path d="m4.93 19.07 1.55-1.55" />
      <path d="m17.52 6.48 1.55-1.55" />
    </svg>
  );
}

function MoonIcon() {
  return (
    <svg aria-hidden className="h-4 w-4" fill="none" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.8" viewBox="0 0 24 24">
      <path d="M20 15.2A7.8 7.8 0 1 1 8.8 4a8.4 8.4 0 0 0 11.2 11.2Z" />
    </svg>
  );
}

interface ThemeToggleProps {
  compact?: boolean;
}

export function ThemeToggle({ compact = false }: ThemeToggleProps) {
  const [theme, setTheme] = useState<ThemeMode>(() => {
    if (typeof window === "undefined") {
      return "light";
    }

    return localStorage.getItem("zenith-theme") === "dark" ? "dark" : "light";
  });

  useEffect(() => {
    applyTheme(theme);
  }, [theme]);

  const nextTheme: ThemeMode = theme === "light" ? "dark" : "light";

  return (
    <button
      type="button"
      aria-label={`Ativar tema ${nextTheme === "light" ? "claro" : "escuro"}`}
      aria-pressed={theme === "dark"}
      className={cn(
        "focusable inline-flex items-center rounded-full border border-[var(--surface-edge)] bg-[var(--card-strong)] text-[var(--text-secondary)] shadow-[var(--elevated-shadow)] transition-all duration-200 hover:border-[var(--accent)] hover:text-[var(--text-primary)]",
        compact ? "h-11 w-[74px] justify-center px-2" : "h-12 min-w-[188px] justify-between px-3.5 text-sm font-medium",
      )}
      onClick={() => {
        const updated = theme === "light" ? "dark" : "light";
        setTheme(updated);
        applyTheme(updated);
      }}
    >
      {!compact ? <span className="text-sm font-medium text-[var(--text-primary)]">{theme === "light" ? "Tema claro" : "Tema escuro"}</span> : null}
      <span
        className={cn(
          "relative inline-flex h-8 w-[58px] items-center rounded-full border transition-all duration-200",
          theme === "dark"
            ? "border-[var(--accent)] bg-[color-mix(in_srgb,var(--accent)_24%,transparent)]"
            : "border-[var(--border)] bg-[var(--bg-elevated)]",
        )}
      >
        <span
          className={cn(
            "absolute top-1 grid h-6 w-6 place-items-center rounded-full text-[var(--text-primary)] shadow-[0_8px_18px_rgba(15,23,42,0.18)] transition-all duration-200",
            theme === "dark"
              ? "left-[29px] bg-[var(--accent)] text-white"
              : "left-1 bg-white",
          )}
        >
          {theme === "light" ? <SunIcon /> : <MoonIcon />}
        </span>
      </span>
    </button>
  );
}
