"use client";

import Link from "next/link";
import { useState } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { BrandWordmark } from "@/components/brand/BrandWordmark";
import { ThemeToggle } from "@/components/theme/ThemeToggle";
import { Alert, Button, Field, Input } from "@/components/ui";
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
              <Field label="Email" htmlFor="email" error={form.formState.errors.email?.message}>
                <Input
                  id="email"
                  type="email"
                  autoComplete="email"
                  invalid={!!form.formState.errors.email}
                  {...form.register("email")}
                />
              </Field>
              {mutation.isError ? (
                <Alert tone="danger">Ocorreu um erro. Tente novamente.</Alert>
              ) : null}
              <Button type="submit" className="w-full" disabled={mutation.isPending}>
                {mutation.isPending ? "Enviando..." : "Enviar link de redefinição"}
              </Button>
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
