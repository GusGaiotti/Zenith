"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Suspense } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { BrandWordmark } from "@/components/brand/BrandWordmark";
import { ThemeToggle } from "@/components/theme/ThemeToggle";
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
    mutation.mutate({ token, newPassword: values.newPassword }, {
      onSuccess: () => {
        router.push("/login?reset=success");
      },
    });
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
        <label className="block text-sm">
          <span className="mb-1.5 block font-medium text-[var(--text-secondary)]">Nova senha</span>
          <input
            type="password"
            autoComplete="new-password"
            className="focusable h-11 w-full rounded-xl border border-[var(--border)] bg-[var(--bg-elevated)] px-3.5 outline-none transition-colors duration-150"
            {...form.register("newPassword")}
          />
          {form.formState.errors.newPassword ? (
            <span className="mt-1.5 block text-xs text-red-300">{form.formState.errors.newPassword.message}</span>
          ) : null}
        </label>
        <label className="block text-sm">
          <span className="mb-1.5 block font-medium text-[var(--text-secondary)]">Confirmar senha</span>
          <input
            type="password"
            autoComplete="new-password"
            className="focusable h-11 w-full rounded-xl border border-[var(--border)] bg-[var(--bg-elevated)] px-3.5 outline-none transition-colors duration-150"
            {...form.register("confirmPassword")}
          />
          {form.formState.errors.confirmPassword ? (
            <span className="mt-1.5 block text-xs text-red-300">{form.formState.errors.confirmPassword.message}</span>
          ) : null}
        </label>
        {mutation.isError ? (
          <p className="danger-chip rounded-xl px-3 py-2.5 text-sm">
            Link inválido ou expirado. Solicite um novo link de redefinição.
          </p>
        ) : null}
        <button
          disabled={mutation.isPending}
          className="focusable h-11 w-full rounded-xl bg-[var(--accent)] px-4 font-semibold text-white shadow-[0_8px_24px_rgba(79,124,255,0.35)] transition-all duration-150 hover:bg-[var(--accent-hover)] disabled:cursor-not-allowed disabled:opacity-45"
        >
          {mutation.isPending ? "Salvando..." : "Salvar nova senha"}
        </button>
      </form>
    </div>
  );
}

export default function ResetPasswordPage() {
  return (
    <main className="relative flex min-h-screen items-center justify-center overflow-hidden p-4 sm:p-6">
      <div className="absolute right-4 top-4 z-10">
        <div className="w-[190px]">
          <ThemeToggle />
        </div>
      </div>
      <section className="surface w-full max-w-md p-8 sm:p-10">
        <BrandWordmark labelClassName="text-4xl text-[var(--text-primary)]" />
        <Suspense fallback={<div className="mt-8 text-sm text-[var(--text-secondary)]">Carregando...</div>}>
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
