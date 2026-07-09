import type { ReactNode } from "react";

type FieldProps = {
  label: string;
  htmlFor?: string;
  error?: string;
  hint?: string;
  children: ReactNode;
};

export function Field({ label, htmlFor, error, hint, children }: FieldProps) {
  return (
    <div className="space-y-1.5">
      <label htmlFor={htmlFor} className="block text-sm font-medium text-[var(--text-secondary)]">
        {label}
      </label>
      {children}
      {hint && !error ? <p className="text-xs text-[var(--text-muted)]">{hint}</p> : null}
      {error ? <p className="text-xs text-[var(--danger-text)]">{error}</p> : null}
    </div>
  );
}
