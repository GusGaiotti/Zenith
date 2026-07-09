"use client";

import Link from "next/link";
import { useState } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { BrandWordmark } from "@/components/brand/BrandWordmark";
import { ThemeToggle } from "@/components/theme/ThemeToggle";
import { useForgotPassword } from "@/hooks/useAuth";
import { forgotPasswordSchema, type ForgotPasswordSchema } from "@/lib/validators/auth.schemas";

export default function ForgotPasswordPage() {
  const mutation = useForgotPassword();
  const [submitted, setSubmitted] = useState(false);

  const form = useForm<ForgotPasswordSchema>({
    resolver: zodResolver(forgotPasswordSchema),
    defaultValues: { email: "" },
  });

  const onSubmit = form.handleSubmit((values) => {
    mutation.mutate(values.email, {
      onSuccess: () => setSubmitted(true),
    });
  });

  return (
    <main className="relative flex min-h-screen items-center justify-center overflow-hidden p-4 sm:p-6">
      <div className="absolute top-4 right-4 z-10">
        <div className="w-[190px]">
          <ThemeToggle />
        </div>
      </div>
      <section className="surface w-full max-w-md p-8 sm:p-10">
        <BrandWordmark labelClassName="text-4xl text-[var(--text-primary)]" />

        {submitted ? (
          <div className="mt-8">
            <h1 className="text-2xl font-semibold text-[var(--text-primary)]">
              Verifique seu email
            </h1>
            <p className="mt-3 text-sm text-[var(--text-secondary)]">
              Se existe uma conta com esse endereço, você receberá um link para redefinir sua senha
              em breve. O link expira em 1 hora.
            </p>
            <p className="mt-4 text-sm text-[var(--text-secondary)]">
              Não recebeu?{" "}
              <button
                className="font-semibold text-[var(--accent-hover)] hover:underline"
                onClick={() => setSubmitted(false)}
              >
                Tentar novamente
              </button>
            </p>
          </div>
        ) : (
          <div className="mt-8">
            <h1 className="text-2xl font-semibold text-[var(--text-primary)]">Redefinir senha</h1>
            <p className="mt-2 text-sm text-[var(--text-secondary)]">
              Informe seu email e enviaremos um link para criar uma nova senha.
            </p>

            <form className="mt-6 space-y-5" onSubmit={onSubmit}>
              <label className="block text-sm">
                <span className="mb-1.5 block font-medium text-[var(--text-secondary)]">Email</span>
                <input
                  type="email"
                  autoComplete="email"
                  className="focusable h-11 w-full rounded-xl border border-[var(--border)] bg-[var(--bg-elevated)] px-3.5 transition-colors duration-150 outline-none"
                  {...form.register("email")}
                />
                {form.formState.errors.email ? (
                  <span className="mt-1.5 block text-xs text-[var(--danger-text)]">
                    {form.formState.errors.email.message}
                  </span>
                ) : null}
              </label>
              {mutation.isError ? (
                <p className="danger-chip rounded-xl px-3 py-2.5 text-sm">
                  Ocorreu um erro. Tente novamente.
                </p>
              ) : null}
              <button
                disabled={mutation.isPending}
                className="focusable h-11 w-full rounded-xl bg-[var(--accent)] px-4 font-semibold text-white shadow-[0_8px_24px_rgba(79,124,255,0.35)] transition-all duration-150 hover:bg-[var(--accent-hover)] disabled:cursor-not-allowed disabled:opacity-45"
              >
                {mutation.isPending ? "Enviando..." : "Enviar link de redefinição"}
              </button>
            </form>
          </div>
        )}

        <p className="mt-6 text-sm text-[var(--text-secondary)]">
          <Link className="font-semibold text-[var(--accent-hover)]" href="/login">
            Voltar para o login
          </Link>
        </p>
      </section>
    </main>
  );
}
