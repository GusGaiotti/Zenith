interface LoadingSpinnerProps {
  label?: string;
  size?: "sm" | "md" | "lg";
}

const sizeClassNames = {
  sm: "h-4 w-4 border-2",
  md: "h-7 w-7 border-2",
  lg: "h-10 w-10 border-[3px]",
} as const;

export function LoadingSpinner({ label, size = "md" }: LoadingSpinnerProps) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 text-center">
      <span
        aria-hidden
        className={`${sizeClassNames[size]} inline-block animate-spin rounded-full border-[rgba(120,156,255,0.22)] border-t-[var(--accent)]`}
      />
      {label ? <p className="text-sm text-[var(--text-secondary)]">{label}</p> : null}
    </div>
  );
}
