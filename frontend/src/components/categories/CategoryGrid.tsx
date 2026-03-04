"use client";

import type { CategoryResponse } from "@/types/api";

interface CategoryGridProps {
  categories: CategoryResponse[];
  onEdit: (category: CategoryResponse) => void;
  onDelete: (category: CategoryResponse) => void;
  deletingId?: number | null;
}

export function CategoryGrid({ categories, onEdit, onDelete, deletingId }: CategoryGridProps) {
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
                className="focusable rounded-full border border-[var(--surface-edge)] px-3 py-1.5 text-xs text-[var(--text-secondary)] transition-colors duration-150 hover:border-[var(--accent)] hover:text-[var(--text-primary)]"
                onClick={() => onEdit(category)}
              >
                Editar
              </button>
              <button
                type="button"
                className="focusable rounded-full border border-red-500/30 px-3 py-1.5 text-xs text-red-300 transition-colors duration-150 hover:border-red-400/60 hover:bg-red-500/10"
                onClick={() => onDelete(category)}
              >
                {deletingId === category.id ? "Excluindo..." : "Excluir"}
              </button>
            </div>
          </div>
        </article>
      ))}
    </div>
  );
}
