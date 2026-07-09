"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Suspense } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { BrandWordmark } from "@/components/brand/BrandWordmark";
import { ThemeToggle } from "@/components/theme/ThemeToggle";
import { Alert, Button, Field, Input } from "@/components/ui";
import { useResetPassword } from "@/hooks/useAuth";
import { resetPasswordSchema, type ResetPasswordSchema } from "@/lib/validators/auth.schemas";

function ResetPasswordForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const token = searchParams.get("token") ?? "";
  const mutation = useResetPassword();

  const form = useForm<ResetPasswordSchema>({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: { newPassword: "", confirmPassword: "" },
  });

  const onSubmit = form.handleSubmit((values) => {
    mutation.mutate(
      { token, newPassword: values.newPassword },
      {
        onSuccess: () => {
          router.push("/login?reset=success");
        },
      },
    );
  });

  if (!token) {
    return (
      <div className="mt-8">
        <p className="text-sm text-[var(--text-secondary)]">
          Link inválido ou expirado.{" "}
          <Link className="font-semibold text-[var(--accent-hover)]" href="/forgot-password">
            Solicitar novo link
          </Link>
        </p>
      </div>
    );
  }

  return (
    <div className="mt-8">
      <h1 className="text-2xl font-semibold text-[var(--text-primary)]">Nova senha</h1>
      <p className="mt-2 text-sm text-[var(--text-secondary)]">
        Escolha uma senha com pelo menos 6 caracteres.
      </p>

      <form className="mt-6 space-y-5" onSubmit={onSubmit}>
        <Field
          label="Nova senha"
          htmlFor="newPassword"
          error={form.formState.errors.newPassword?.message}
        >
          <Input
            id="newPassword"
            type="password"
            autoComplete="new-password"
            invalid={!!form.formState.errors.newPassword}
            {...form.register("newPassword")}
          />
        </Field>
        <Field
          label="Confirmar senha"
          htmlFor="confirmPassword"
          error={form.formState.errors.confirmPassword?.message}
        >
          <Input
            id="confirmPassword"
            type="password"
            autoComplete="new-password"
            invalid={!!form.formState.errors.confirmPassword}
            {...form.register("confirmPassword")}
          />
        </Field>
        {mutation.isError ? (
          <Alert tone="danger">
            Link inválido ou expirado. Solicite um novo link de redefinição.
          </Alert>
        ) : null}
        <Button type="submit" className="w-full" disabled={mutation.isPending}>
          {mutation.isPending ? "Salvando..." : "Salvar nova senha"}
        </Button>
      </form>
    </div>
  );
}

export default function ResetPasswordPage() {
  return (
    <main className="relative flex min-h-screen items-center justify-center overflow-hidden p-4 sm:p-6">
      <div className="absolute top-4 right-4 z-10">
        <div className="w-[190px]">
          <ThemeToggle />
        </div>
      </div>
      <section className="surface w-full max-w-md p-8 sm:p-10">
        <BrandWordmark labelClassName="text-4xl text-[var(--text-primary)]" />
        <Suspense
          fallback={<div className="mt-8 text-sm text-[var(--text-secondary)]">Carregando...</div>}
        >
          <ResetPasswordForm />
        </Suspense>
        <p className="mt-6 text-sm text-[var(--text-secondary)]">
          <Link className="font-semibold text-[var(--accent-hover)]" href="/login">
            Voltar para o login
          </Link>
        </p>
      </section>
    </main>
  );
}
