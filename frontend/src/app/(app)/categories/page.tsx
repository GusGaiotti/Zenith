"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { CategoryFormModal } from "@/components/categories/CategoryFormModal";
import { CategoryGrid } from "@/components/categories/CategoryGrid";
import { EmptyState } from "@/components/shared/EmptyState";
import { LoadingSkeleton } from "@/components/shared/LoadingSkeleton";
import { PageHeader } from "@/components/shared/PageHeader";
import { Alert, Button } from "@/components/ui";
import {
  useCategories,
  useCreateCategory,
  useDeleteCategory,
  useUpdateCategory,
} from "@/hooks/useCategories";
import { useAuthStore } from "@/lib/store/auth.store";
import { getApiErrorMessage } from "@/lib/utils/api-error";
import type { CategoryResponse } from "@/types/api";

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
        <PageHeader title="Categorias" subtitle="Organize gastos com categorias por cor." />
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
          <Button
            size="sm"
            onClick={() => {
              setEditingCategory(null);
              setErrorMessage(null);
              setOpen(true);
            }}
          >
            Nova categoria
          </Button>
        }
      />

      {errorMessage ? (
        <Alert tone="danger" className="mb-4">
          {errorMessage}
        </Alert>
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
                  getApiErrorMessage(
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
                  setErrorMessage(
                    getApiErrorMessage(error, "Não foi possível atualizar a categoria."),
                  );
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
              setErrorMessage(getApiErrorMessage(error, "Não foi possível criar a categoria."));
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
              <Button onClick={() => setDeleteErrorMessage(null)}>Fechar</Button>
            </div>
          </section>
        </div>
      ) : null}
    </div>
  );
}
