"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { PageHeader } from "@/components/shared/PageHeader";
import { Alert, Button, Field, Input } from "@/components/ui";
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
          <Field label="Email" htmlFor="email" hint="O email não pode ser alterado.">
            <Input
              id="email"
              type="email"
              disabled
              value={profile?.email ?? ""}
              className="text-[var(--text-muted)] opacity-60"
            />
          </Field>
          <Field
            label="Nome de exibição"
            htmlFor="displayName"
            error={profileForm.formState.errors.displayName?.message}
          >
            <Input
              id="displayName"
              type="text"
              invalid={!!profileForm.formState.errors.displayName}
              {...profileForm.register("displayName")}
            />
          </Field>
          {updateProfile.isError && !passwordForm.formState.isSubmitting ? (
            <Alert tone="danger">
              {getApiErrorMessage(updateProfile.error, "Ocorreu um erro. Tente novamente.")}
            </Alert>
          ) : null}
          {profileSuccess ? <Alert tone="success">Perfil atualizado com sucesso.</Alert> : null}
          <Button type="submit" disabled={updateProfile.isPending}>
            {updateProfile.isPending ? "Salvando..." : "Salvar"}
          </Button>
        </form>
      </section>

      <section className="surface p-6 sm:p-8">
        <h2 className="text-base font-semibold text-[var(--text-primary)]">Alterar senha</h2>
        <p className="mt-1 text-sm text-[var(--text-secondary)]">
          Escolha uma senha com pelo menos 6 caracteres.
        </p>

        <form className="mt-5 space-y-4" onSubmit={onSavePassword}>
          <Field
            label="Senha atual"
            htmlFor="currentPassword"
            error={passwordForm.formState.errors.currentPassword?.message}
          >
            <Input
              id="currentPassword"
              type="password"
              autoComplete="current-password"
              invalid={!!passwordForm.formState.errors.currentPassword}
              {...passwordForm.register("currentPassword")}
            />
          </Field>
          <Field
            label="Nova senha"
            htmlFor="newPassword"
            error={passwordForm.formState.errors.newPassword?.message}
          >
            <Input
              id="newPassword"
              type="password"
              autoComplete="new-password"
              invalid={!!passwordForm.formState.errors.newPassword}
              {...passwordForm.register("newPassword")}
            />
          </Field>
          <Field
            label="Confirmar nova senha"
            htmlFor="confirmPassword"
            error={passwordForm.formState.errors.confirmPassword?.message}
          >
            <Input
              id="confirmPassword"
              type="password"
              autoComplete="new-password"
              invalid={!!passwordForm.formState.errors.confirmPassword}
              {...passwordForm.register("confirmPassword")}
            />
          </Field>
          {updateProfile.isError && passwordForm.formState.isSubmitting ? (
            <Alert tone="danger">
              {getApiErrorMessage(updateProfile.error, "Ocorreu um erro. Tente novamente.")}
            </Alert>
          ) : null}
          {passwordSuccess ? <Alert tone="success">Senha alterada com sucesso.</Alert> : null}
          <Button type="submit" disabled={updateProfile.isPending}>
            {updateProfile.isPending ? "Salvando..." : "Alterar senha"}
          </Button>
        </form>
      </section>

      <section className="surface border-[color-mix(in_srgb,var(--expense)_30%,var(--border))] p-6 sm:p-8">
        <h2 className="text-base font-semibold text-[var(--expense)]">Zona de perigo</h2>
        <p className="mt-1 text-sm text-[var(--text-secondary)]">
          Excluir sua conta é uma ação irreversível. Todos os seus dados serão removidos.
        </p>
        <Button variant="danger" className="mt-4" onClick={() => setShowDeleteConfirm(true)}>
          Excluir minha conta
        </Button>
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
