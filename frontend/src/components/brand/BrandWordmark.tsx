"use client";

import { cn } from "@/lib/utils/cn";

interface BrandWordmarkProps {
  animate?: boolean;
  compact?: boolean;
  className?: string;
  labelClassName?: string;
}

const letters = ["e", "n", "i", "t", "h"];

export function BrandWordmark({
  animate = false,
  compact = false,
  className,
  labelClassName,
}: BrandWordmarkProps) {
  return (
    <div className={cn("inline-flex items-center gap-1.5", className)}>
      <span className={cn("brand-z-cut relative inline-flex items-center justify-center font-display font-semibold", compact ? "text-2xl" : "text-4xl")}>
        Z
      </span>
      {!compact ? (
        <span className={cn("inline-flex items-center font-display font-semibold tracking-[-0.04em] text-[var(--text-primary)]", labelClassName)}>
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
