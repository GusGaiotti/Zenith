import { cva, type VariantProps } from "class-variance-authority";
import type { HTMLAttributes } from "react";
import { cn } from "@/lib/utils/cn";

const alertVariants = cva("rounded-xl border px-4 py-3 text-sm", {
  variants: {
    tone: {
      danger: "border-[var(--danger-border)] bg-[var(--danger-bg)] text-[var(--danger-text)]",
      success:
        "border-[color-mix(in_srgb,var(--income)_26%,transparent)] bg-[color-mix(in_srgb,var(--income)_10%,transparent)] text-[var(--income)]",
      info: "border-[var(--border)] bg-[var(--panel-bg)] text-[var(--text-secondary)]",
    },
  },
  defaultVariants: {
    tone: "danger",
  },
});

type AlertProps = HTMLAttributes<HTMLDivElement> & VariantProps<typeof alertVariants>;

export function Alert({ className, tone, role = "status", ...props }: AlertProps) {
  return <div role={role} className={cn(alertVariants({ tone }), className)} {...props} />;
}
