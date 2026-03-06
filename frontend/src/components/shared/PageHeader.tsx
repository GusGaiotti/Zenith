import type { ReactNode } from "react";

interface PageHeaderProps {
  title: string;
  subtitle?: string;
  actions?: ReactNode;
}

export function PageHeader({ title, subtitle, actions }: PageHeaderProps) {
  return (
    <header className="mb-6 flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
      <div className="min-w-0">
        <h1 className="font-display text-3xl leading-tight text-[var(--text-primary)] md:text-4xl">{title}</h1>
        {subtitle ? <p className="mt-2 text-sm text-[var(--text-secondary)] md:text-[0.95rem]">{subtitle}</p> : null}
      </div>
      {actions ? <div className="w-full min-w-0 md:w-auto">{actions}</div> : null}
    </header>
  );
}

