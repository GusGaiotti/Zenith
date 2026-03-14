"use client";

import { cn } from "@/lib/utils/cn";

interface BrandWordmarkProps {
  animate?: boolean;
  compact?: boolean;
  className?: string;
  labelClassName?: string;
}

const letters = ["e", "n", "i", "t", "h"];

function ZenithMonogram({ compact = false }: { compact?: boolean }) {
  return (
    <span className={cn("inline-flex items-center justify-center", compact ? "h-9 w-9" : "h-14 w-14")}>
      <svg
        aria-hidden
        className={cn("text-[var(--accent)] drop-shadow-[0_10px_22px_rgba(37,99,235,0.24)]", compact ? "h-9 w-9" : "h-14 w-14")}
        fill="none"
        viewBox="0 0 56 56"
      >
        <path
          d="M11 14h34L19 42h26"
          stroke="currentColor"
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth="5.5"
        />
        <path
          d="M28 9v38"
          stroke="currentColor"
          strokeLinecap="round"
          strokeWidth="4"
        />
      </svg>
    </span>
  );
}

export function BrandWordmark({
  animate = false,
  compact = false,
  className,
  labelClassName,
}: BrandWordmarkProps) {
  return (
    <div className={cn("inline-flex items-center gap-1.5", className)}>
      <ZenithMonogram compact={compact} />
      {!compact ? (
        <span className={cn("inline-flex items-center font-display font-semibold tracking-[-0.05em] text-[var(--text-primary)]", labelClassName)}>
          {letters.map((letter, index) => (
            <span
              key={letter}
              className={animate ? "brand-letter-reveal" : undefined}
              style={animate ? { animationDelay: `${120 + index * 70}ms` } : undefined}
            >
              {letter}
            </span>
          ))}
        </span>
      ) : null}
    </div>
  );
}
