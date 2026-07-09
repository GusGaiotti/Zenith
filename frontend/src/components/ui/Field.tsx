import type { ReactNode } from "react";

type FieldProps = {
  label: string;
  htmlFor?: string;
  error?: string;
  hint?: string;
  action?: ReactNode;
  children: ReactNode;
};

export function Field({ label, htmlFor, error, hint, action, children }: FieldProps) {
  return (
    <div className="space-y-1.5">
      <div className="flex items-center justify-between gap-3">
        <label htmlFor={htmlFor} className="block text-sm font-medium text-[var(--text-secondary)]">
          {label}
        </label>
        {action}
      </div>
      {children}
      {hint && !error ? <p className="text-xs text-[var(--text-muted)]">{hint}</p> : null}
      {error ? <p className="text-xs text-[var(--danger-text)]">{error}</p> : null}
    </div>
  );
}
