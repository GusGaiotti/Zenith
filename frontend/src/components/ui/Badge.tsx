import { cva, type VariantProps } from "class-variance-authority";
import type { HTMLAttributes } from "react";
import { cn } from "@/lib/utils/cn";

const badgeVariants = cva(
  "inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-medium",
  {
    variants: {
      tone: {
        neutral: "border-[var(--border)] bg-[var(--bg-elevated)] text-[var(--text-secondary)]",
        income: "border-transparent bg-[var(--accent-muted)] text-[var(--income)]",
        expense: "border-[var(--danger-border)] bg-[var(--danger-bg)] text-[var(--danger-text)]",
        accent: "border-transparent bg-[var(--accent-muted)] text-[var(--accent)]",
      },
    },
    defaultVariants: {
      tone: "neutral",
    },
  },
);

type BadgeProps = HTMLAttributes<HTMLSpanElement> & VariantProps<typeof badgeVariants>;

export function Badge({ className, tone, ...props }: BadgeProps) {
  return <span className={cn(badgeVariants({ tone }), className)} {...props} />;
}
