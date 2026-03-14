interface LoadingSpinnerProps {
  label?: string;
  size?: "sm" | "md" | "lg";
}

const sizeClassNames = {
  sm: "h-4 w-4 border-2",
  md: "h-7 w-7 border-[2.5px]",
  lg: "h-10 w-10 border-[3px]",
} as const;

export function LoadingSpinner({ label, size = "md" }: LoadingSpinnerProps) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 text-center">
      <span className="relative inline-flex items-center justify-center">
        <span
          aria-hidden
          className={`${sizeClassNames[size]} inline-block rounded-full border-[color-mix(in_srgb,var(--accent)_18%,transparent)]`}
        />
        <span
          aria-hidden
          className={`${sizeClassNames[size]} absolute inline-block animate-spin rounded-full border-transparent border-t-[var(--accent)] border-r-[color-mix(in_srgb,var(--accent-emerald)_80%,transparent)]`}
        />
        <span
          aria-hidden
          className="absolute h-1.5 w-1.5 animate-pulse rounded-full bg-[var(--accent-amber)]"
        />
      </span>
      {label ? <p className="text-sm text-[var(--text-secondary)]">{label}</p> : null}
    </div>
  );
}
