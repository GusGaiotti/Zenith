"use client";

import type { CategoryResponse } from "@/types/api";

interface CategoryGridProps {
  categories: CategoryResponse[];
  onEdit: (category: CategoryResponse) => void;
  onDelete: (category: CategoryResponse) => void;
  deletingId?: number | null;
}

export function CategoryGrid({ categories, onEdit, onDelete, deletingId }: CategoryGridProps) {
  function PencilIcon() {
    return (
      <svg aria-hidden className="h-4 w-4" fill="none" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.9" viewBox="0 0 24 24">
        <path d="m4 20 4.2-1 9.3-9.3a2.1 2.1 0 0 0-3-3L5.2 16 4 20Z" />
        <path d="m13.5 7.5 3 3" />
      </svg>
    );
  }

  function TrashIcon() {
    return (
      <svg aria-hidden className="h-4 w-4" fill="none" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.9" viewBox="0 0 24 24">
        <path d="M4.5 7.5h15" />
        <path d="M9.5 3.8h5l.8 1.7H19a1 1 0 0 1 1 1V8H4V6.5a1 1 0 0 1 1-1h3.7l.8-1.7Z" />
        <path d="M8.2 9.5v7.2" />
        <path d="M12 9.5v7.2" />
        <path d="M15.8 9.5v7.2" />
      </svg>
    );
  }

  return (
    <div className="grid gap-4" style={{ gridTemplateColumns: "repeat(auto-fill,minmax(220px,1fr))" }}>
      {categories.map((category) => (
        <article
          key={category.id}
          className="surface group relative overflow-hidden p-4 transition-all duration-150 hover:-translate-y-[2px] hover:shadow-2xl hover:shadow-black/30"
        >
          <div className="absolute inset-y-0 left-0 w-2" style={{ backgroundColor: category.color }} />
          <div className="pl-4">
            <h3 className="font-semibold">{category.name}</h3>
            <p className="mt-1 text-xs text-[var(--text-secondary)]">Criada por {category.createdByDisplayName}</p>
            <div className="mt-4 flex gap-2">
              <button
                type="button"
                className="focusable inline-flex items-center gap-2 rounded-full border border-[color-mix(in_srgb,var(--accent)_28%,transparent)] bg-[color-mix(in_srgb,var(--accent)_10%,var(--panel-bg))] px-3 py-1.5 text-xs font-medium text-[var(--accent-hover)] transition-colors duration-150 hover:border-[var(--accent)] hover:bg-[color-mix(in_srgb,var(--accent)_16%,var(--panel-bg))]"
                onClick={() => onEdit(category)}
              >
                <PencilIcon />
                Editar
              </button>
              <button
                type="button"
                className="focusable inline-flex items-center gap-2 rounded-full border border-[color-mix(in_srgb,var(--expense)_30%,transparent)] bg-[color-mix(in_srgb,var(--expense)_10%,var(--panel-bg))] px-3 py-1.5 text-xs font-medium text-[color-mix(in_srgb,var(--expense)_84%,var(--text-primary))] transition-colors duration-150 hover:border-[color-mix(in_srgb,var(--expense)_45%,transparent)] hover:bg-[color-mix(in_srgb,var(--expense)_14%,var(--panel-bg))]"
                onClick={() => onDelete(category)}
              >
                <TrashIcon />
                {deletingId === category.id ? "Excluindo..." : "Excluir"}
              </button>
            </div>
          </div>
        </article>
      ))}
    </div>
  );
}
