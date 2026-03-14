"use client";

import { AxiosError } from "axios";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { CategoryFormModal } from "@/components/categories/CategoryFormModal";
import { CategoryGrid } from "@/components/categories/CategoryGrid";
import { EmptyState } from "@/components/shared/EmptyState";
import { LoadingSkeleton } from "@/components/shared/LoadingSkeleton";
import { PageHeader } from "@/components/shared/PageHeader";
import { useCategories, useCreateCategory, useDeleteCategory, useUpdateCategory } from "@/hooks/useCategories";
import { useAuthStore } from "@/lib/store/auth.store";
import type { CategoryResponse } from "@/types/api";

function extractErrorMessage(error: unknown, fallback: string) {
  if (error instanceof AxiosError) {
    const message = (error.response?.data as { message?: string } | undefined)?.message;
    if (message?.toLowerCase().includes("associated transactions")) {
      return "Não é possível excluir a categoria porque ela possui transações associadas. Exclua as transações vinculadas antes de tentar novamente.";
    }
    return message || fallback;
  }

  return fallback;
}

export default function CategoriesPage() {
  const router = useRouter();
  const [open, setOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<CategoryResponse | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [deleteErrorMessage, setDeleteErrorMessage] = useState<string | null>(null);
  const [pendingDeleteId, setPendingDeleteId] = useState<number | null>(null);
  const activeLedgerId = useAuthStore((state) => state.activeLedgerId);
  const categories = useCategories();
  const createCategory = useCreateCategory();
  const updateCategory = useUpdateCategory();
  const deleteCategory = useDeleteCategory();

  const isEditing = Boolean(editingCategory);
  const isSubmitting = createCategory.isPending || updateCategory.isPending;

  if (!activeLedgerId) {
    return (
      <div className="pb-20 md:pb-6">
        <PageHeader
          title="Categorias"
          subtitle="Organize gastos com categorias por cor."
        />
        <EmptyState
          title="Nenhuma fatura ativa"
          description="As categorias ficam disponíveis assim que você criar uma fatura ou aceitar um convite."
          action={{ label: "Criar fatura", onClick: () => router.push("/onboarding") }}
        />
      </div>
    );
  }

  return (
    <div className="pb-20 md:pb-6">
      <PageHeader
        title="Categorias"
        subtitle="Organize gastos com categorias por cor."
        actions={
          <button className="focusable rounded-md bg-[var(--accent)] px-3 py-2 text-sm font-medium text-white" onClick={() => {
            setEditingCategory(null);
            setErrorMessage(null);
            setOpen(true);
          }}>
            Nova categoria
          </button>
        }
      />

      {errorMessage ? (
        <div className="mb-4 rounded-md border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-200">
          {errorMessage}
        </div>
      ) : null}

      {categories.isLoading ? <LoadingSkeleton variant="chart" /> : null}
      {!categories.isLoading && (categories.data?.length ?? 0) > 0 ? (
        <CategoryGrid
          categories={categories.data ?? []}
          deletingId={pendingDeleteId}
          onEdit={(category) => {
            setErrorMessage(null);
            setEditingCategory(category);
            setOpen(true);
          }}
          onDelete={(category) => {
            const shouldDelete = window.confirm(`Excluir a categoria "${category.name}"?`);

            if (!shouldDelete) {
              return;
            }

            setErrorMessage(null);
            setDeleteErrorMessage(null);
            setPendingDeleteId(category.id);
            deleteCategory.mutate(category.id, {
              onSuccess: () => {
                setPendingDeleteId(null);
              },
              onError: (error) => {
                setPendingDeleteId(null);
                setDeleteErrorMessage(
                  extractErrorMessage(
                    error,
                    "Não foi possível excluir a categoria. Ela pode estar associada a transações existentes.",
                  ),
                );
              },
            });
          }}
        />
      ) : null}
      {!categories.isLoading && (categories.data?.length ?? 0) === 0 ? (
        <EmptyState
          title="Nenhuma categoria ainda"
          description="Crie sua primeira categoria para organizar os lançamentos."
          action={{ label: "Nova categoria", onClick: () => setOpen(true) }}
        />
      ) : null}

      <CategoryFormModal
        open={open}
        title={isEditing ? "Editar categoria" : "Nova categoria"}
        submitLabel={isEditing ? "Salvar" : "Criar"}
        initialName={editingCategory?.name ?? ""}
        initialColor={editingCategory?.color}
        isSubmitting={isSubmitting}
        onClose={() => {
          setOpen(false);
          setEditingCategory(null);
        }}
        onSubmit={(payload) => {
          setErrorMessage(null);

          if (editingCategory) {
            updateCategory.mutate(
              { id: editingCategory.id, body: payload },
              {
                onSuccess: () => {
                  setOpen(false);
                  setEditingCategory(null);
                },
                onError: (error) => {
                  setErrorMessage(extractErrorMessage(error, "Não foi possível atualizar a categoria."));
                },
              },
            );
            return;
          }

          createCategory.mutate(payload, {
            onSuccess: () => {
              setOpen(false);
              setEditingCategory(null);
            },
            onError: (error) => {
              setErrorMessage(extractErrorMessage(error, "Não foi possível criar a categoria."));
            },
          });
        }}
      />

      {deleteErrorMessage ? (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/55 p-4">
          <section className="surface w-full max-w-md p-6">
            <h3 className="font-display text-3xl italic">Não foi possível excluir</h3>
            <p className="mt-4 text-sm text-[var(--text-secondary)]">{deleteErrorMessage}</p>
            <div className="mt-5 flex justify-end">
              <button
                type="button"
                className="focusable rounded-md bg-[var(--accent)] px-4 py-2 font-medium text-white"
                onClick={() => setDeleteErrorMessage(null)}
              >
                Fechar
              </button>
            </div>
          </section>
        </div>
      ) : null}
    </div>
  );
}

