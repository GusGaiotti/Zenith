import type { InputHTMLAttributes, Ref } from "react";
import { cn } from "@/lib/utils/cn";

type InputProps = InputHTMLAttributes<HTMLInputElement> & {
  invalid?: boolean;
  ref?: Ref<HTMLInputElement>;
};

export function Input({ className, invalid, ...props }: InputProps) {
  return (
    <input
      className={cn(
        "focusable h-11 w-full rounded-xl border bg-[var(--bg-elevated)] px-3.5 transition-colors duration-150 outline-none",
        invalid ? "border-[var(--danger-border)]" : "border-[var(--border)]",
        className,
      )}
      {...props}
    />
  );
}
