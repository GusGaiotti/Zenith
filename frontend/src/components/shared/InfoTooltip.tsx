interface InfoTooltipProps {
  text: string;
  align?: "left" | "right";
}

export function InfoTooltip({ text, align = "right" }: InfoTooltipProps) {
  return (
    <span className="group relative z-20 inline-flex items-center">
      <button
        aria-label={text}
        className="cursor-help p-0"
        type="button"
      >
        <span
          aria-hidden
          className="flex h-6 w-6 items-center justify-center rounded-full border border-[var(--surface-edge)] bg-[var(--bg-elevated)] font-mono text-xs leading-none text-[var(--text-primary)] transition-colors duration-150 group-hover:border-[var(--accent)] group-hover:text-[var(--accent)] group-focus-within:border-[var(--accent)] group-focus-within:text-[var(--accent)]"
        >
          i
        </span>
      </button>
      <span
        className={`pointer-events-none absolute bottom-full z-50 mb-3 w-64 max-w-[calc(100vw-2rem)] translate-y-1 rounded-xl border border-[var(--surface-edge)] bg-[rgba(28,26,23,0.98)] px-3 py-2 text-left text-xs leading-5 text-[var(--text-primary)] break-words opacity-0 shadow-[0_18px_50px_rgba(0,0,0,0.45)] transition-all duration-150 group-hover:translate-y-0 group-hover:opacity-100 group-focus-within:translate-y-0 group-focus-within:opacity-100 ${align === "left" ? "left-0" : "right-0"}`}
        role="tooltip"
      >
        {text}
      </span>
    </span>
  );
}
