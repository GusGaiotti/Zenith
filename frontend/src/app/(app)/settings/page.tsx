"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { PageHeader } from "@/components/shared/PageHeader";
import { useDeleteAccount, useMyProfile, useUpdateProfile } from "@/hooks/useUser";
import { getApiErrorMessage } from "@/lib/utils/api-error";

const profileSchema = z.object({
  displayName: z.string().min(2, "O nome deve ter pelo menos 2 caracteres").max(100),
});

const passwordSchema = z
  .object({
    currentPassword: z.string().min(1, "Senha atual obrigatória"),
    newPassword: z.string().min(6, "A nova senha deve ter pelo menos 6 caracteres"),
    confirmPassword: z.string().min(1, "Confirmação obrigatória"),
  })
  .refine((d) => d.newPassword === d.confirmPassword, {
    message: "As senhas não coincidem",
    path: ["confirmPassword"],
  });

type ProfileSchema = z.infer<typeof profileSchema>;
type PasswordSchema = z.infer<typeof passwordSchema>;

export default function SettingsPage() {
  const router = useRouter();
  const { data: profile } = useMyProfile();
  const updateProfile = useUpdateProfile();
  const deleteAccount = useDeleteAccount();
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [profileSuccess, setProfileSuccess] = useState(false);
  const [passwordSuccess, setPasswordSuccess] = useState(false);

  const profileForm = useForm<ProfileSchema>({
    resolver: zodResolver(profileSchema),
    values: { displayName: profile?.displayName ?? "" },
  });

  const passwordForm = useForm<PasswordSchema>({
    resolver: zodResolver(passwordSchema),
    defaultValues: { currentPassword: "", newPassword: "", confirmPassword: "" },
  });

  const onSaveProfile = profileForm.handleSubmit((values) => {
    setProfileSuccess(false);
    updateProfile.mutate(
      { displayName: values.displayName },
      {
        onSuccess: () => setProfileSuccess(true),
      },
    );
  });

  const onSavePassword = passwordForm.handleSubmit((values) => {
    setPasswordSuccess(false);
    updateProfile.mutate(
      { currentPassword: values.currentPassword, newPassword: values.newPassword },
      {
        onSuccess: () => {
          setPasswordSuccess(true);
          passwordForm.reset();
        },
      },
    );
  });

  return (
    <div className="mx-auto max-w-2xl space-y-8 px-2 py-4 sm:px-0">
      <PageHeader title="Configurações" subtitle="Gerencie suas informações de conta." />

      <section className="surface p-6 sm:p-8">
        <h2 className="text-base font-semibold text-[var(--text-primary)]">Perfil</h2>
        <p className="mt-1 text-sm text-[var(--text-secondary)]">Atualize seu nome de exibição.</p>

        <form className="mt-5 space-y-4" onSubmit={onSaveProfile}>
          <div>
            <label className="block text-sm">
              <span className="mb-1.5 block font-medium text-[var(--text-secondary)]">Email</span>
              <input
                type="email"
                disabled
                value={profile?.email ?? ""}
                className="h-11 w-full rounded-xl border border-[var(--border)] bg-[var(--bg-elevated)] px-3.5 text-[var(--text-muted)] opacity-60"
              />
            </label>
            <p className="mt-1 text-xs text-[var(--text-muted)]">O email não pode ser alterado.</p>
          </div>
          <label className="block text-sm">
            <span className="mb-1.5 block font-medium text-[var(--text-secondary)]">
              Nome de exibição
            </span>
            <input
              type="text"
              className="focusable h-11 w-full rounded-xl border border-[var(--border)] bg-[var(--bg-elevated)] px-3.5 transition-colors duration-150 outline-none"
              {...profileForm.register("displayName")}
            />
            {profileForm.formState.errors.displayName ? (
              <span className="mt-1.5 block text-xs text-[var(--danger-text)]">
                {profileForm.formState.errors.displayName.message}
              </span>
            ) : null}
          </label>
          {updateProfile.isError && !passwordForm.formState.isSubmitting ? (
            <p className="danger-chip rounded-xl px-3 py-2.5 text-sm">
              {getApiErrorMessage(updateProfile.error, "Ocorreu um erro. Tente novamente.")}
            </p>
          ) : null}
          {profileSuccess ? (
            <p className="rounded-xl border border-[color-mix(in_srgb,var(--income)_26%,transparent)] bg-[color-mix(in_srgb,var(--income)_10%,transparent)] px-3 py-2.5 text-sm text-[var(--income)]">
              Perfil atualizado com sucesso.
            </p>
          ) : null}
          <button
            disabled={updateProfile.isPending}
            className="focusable h-10 rounded-xl bg-[var(--accent)] px-5 text-sm font-semibold text-white transition-all duration-150 hover:bg-[var(--accent-hover)] disabled:cursor-not-allowed disabled:opacity-45"
          >
            {updateProfile.isPending ? "Salvando..." : "Salvar"}
          </button>
        </form>
      </section>

      <section className="surface p-6 sm:p-8">
        <h2 className="text-base font-semibold text-[var(--text-primary)]">Alterar senha</h2>
        <p className="mt-1 text-sm text-[var(--text-secondary)]">
          Escolha uma senha com pelo menos 6 caracteres.
        </p>

        <form className="mt-5 space-y-4" onSubmit={onSavePassword}>
          <label className="block text-sm">
            <span className="mb-1.5 block font-medium text-[var(--text-secondary)]">
              Senha atual
            </span>
            <input
              type="password"
              autoComplete="current-password"
              className="focusable h-11 w-full rounded-xl border border-[var(--border)] bg-[var(--bg-elevated)] px-3.5 transition-colors duration-150 outline-none"
              {...passwordForm.register("currentPassword")}
            />
            {passwordForm.formState.errors.currentPassword ? (
              <span className="mt-1.5 block text-xs text-[var(--danger-text)]">
                {passwordForm.formState.errors.currentPassword.message}
              </span>
            ) : null}
          </label>
          <label className="block text-sm">
            <span className="mb-1.5 block font-medium text-[var(--text-secondary)]">
              Nova senha
            </span>
            <input
              type="password"
              autoComplete="new-password"
              className="focusable h-11 w-full rounded-xl border border-[var(--border)] bg-[var(--bg-elevated)] px-3.5 transition-colors duration-150 outline-none"
              {...passwordForm.register("newPassword")}
            />
            {passwordForm.formState.errors.newPassword ? (
              <span className="mt-1.5 block text-xs text-[var(--danger-text)]">
                {passwordForm.formState.errors.newPassword.message}
              </span>
            ) : null}
          </label>
          <label className="block text-sm">
            <span className="mb-1.5 block font-medium text-[var(--text-secondary)]">
              Confirmar nova senha
            </span>
            <input
              type="password"
              autoComplete="new-password"
              className="focusable h-11 w-full rounded-xl border border-[var(--border)] bg-[var(--bg-elevated)] px-3.5 transition-colors duration-150 outline-none"
              {...passwordForm.register("confirmPassword")}
            />
            {passwordForm.formState.errors.confirmPassword ? (
              <span className="mt-1.5 block text-xs text-[var(--danger-text)]">
                {passwordForm.formState.errors.confirmPassword.message}
              </span>
            ) : null}
          </label>
          {updateProfile.isError && passwordForm.formState.isSubmitting ? (
            <p className="danger-chip rounded-xl px-3 py-2.5 text-sm">
              {getApiErrorMessage(updateProfile.error, "Ocorreu um erro. Tente novamente.")}
            </p>
          ) : null}
          {passwordSuccess ? (
            <p className="rounded-xl border border-[color-mix(in_srgb,var(--income)_26%,transparent)] bg-[color-mix(in_srgb,var(--income)_10%,transparent)] px-3 py-2.5 text-sm text-[var(--income)]">
              Senha alterada com sucesso.
            </p>
          ) : null}
          <button
            disabled={updateProfile.isPending}
            className="focusable h-10 rounded-xl bg-[var(--accent)] px-5 text-sm font-semibold text-white transition-all duration-150 hover:bg-[var(--accent-hover)] disabled:cursor-not-allowed disabled:opacity-45"
          >
            {updateProfile.isPending ? "Salvando..." : "Alterar senha"}
          </button>
        </form>
      </section>

      <section className="surface border-[color-mix(in_srgb,var(--expense)_30%,var(--border))] p-6 sm:p-8">
        <h2 className="text-base font-semibold text-[var(--expense)]">Zona de perigo</h2>
        <p className="mt-1 text-sm text-[var(--text-secondary)]">
          Excluir sua conta é uma ação irreversível. Todos os seus dados serão removidos.
        </p>
        <button
          className="focusable danger-chip mt-4 h-10 rounded-xl px-5 text-sm font-semibold transition-all duration-150"
          onClick={() => setShowDeleteConfirm(true)}
        >
          Excluir minha conta
        </button>
      </section>

      <ConfirmDialog
        open={showDeleteConfirm}
        title="Excluir conta"
        description="Tem certeza que deseja excluir sua conta? Todos os seus dados serão removidos permanentemente. Esta ação não pode ser desfeita."
        confirmLabel="Excluir conta"
        variant="danger"
        isPending={deleteAccount.isPending}
        onConfirm={() => {
          deleteAccount.mutate(undefined, {
            onSuccess: () => router.replace("/login"),
          });
        }}
        onCancel={() => setShowDeleteConfirm(false)}
      />
    </div>
  );
}
