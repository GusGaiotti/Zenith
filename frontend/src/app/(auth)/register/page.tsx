"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm, useWatch } from "react-hook-form";
import { BrandWordmark } from "@/components/brand/BrandWordmark";
import { ThemeToggle } from "@/components/theme/ThemeToggle";
import { Alert, Button, Field, Input } from "@/components/ui";
import { useRegister } from "@/hooks/useAuth";
import { registerSchema, type RegisterSchema } from "@/lib/validators/auth.schemas";

export default function RegisterPage() {
  const router = useRouter();
  const registerMutation = useRegister();
  const [cooldownSeconds, setCooldownSeconds] = useState(0);

  useEffect(() => {
    if (cooldownSeconds <= 0) return;
    const timer = setInterval(() => {
      setCooldownSeconds((value) => Math.max(0, value - 1));
    }, 1000);
    return () => clearInterval(timer);
  }, [cooldownSeconds]);

  const form = useForm<RegisterSchema>({
    resolver: zodResolver(registerSchema),
    defaultValues: { displayName: "", email: "", password: "" },
  });

  const onSubmit = form.handleSubmit((values) => {
    registerMutation.mutate(values, {
      onSuccess: () => {
        setCooldownSeconds(0);
        router.push("/onboarding");
      },
      onError: () => {
        setCooldownSeconds(5);
      },
    });
  });

  const password = useWatch({ control: form.control, name: "password", defaultValue: "" });
  const strength = password.length > 10 ? "Forte" : password.length > 6 ? "Média" : "Fraca";

  return (
    <main className="relative flex min-h-screen items-center justify-center p-4">
      <div className="absolute top-4 right-4 z-10">
        <div className="w-[190px]">
          <ThemeToggle />
        </div>
      </div>
      <section className="surface w-full max-w-md p-8">
        <BrandWordmark
          animate
          className="mb-6"
          labelClassName="text-5xl text-[var(--text-primary)]"
        />
        <h1 className="font-display text-4xl">Criar conta</h1>
        <p className="mt-2 text-sm text-[var(--text-secondary)]">
          Configure o Zenith em menos de um minuto.
        </p>
        <form className="mt-6 space-y-4" onSubmit={onSubmit}>
          <Field
            label="Nome"
            htmlFor="displayName"
            error={form.formState.errors.displayName?.message}
          >
            <Input
              id="displayName"
              invalid={!!form.formState.errors.displayName}
              {...form.register("displayName")}
            />
          </Field>
          <Field label="Email" htmlFor="email" error={form.formState.errors.email?.message}>
            <Input
              id="email"
              type="email"
              invalid={!!form.formState.errors.email}
              {...form.register("email")}
            />
          </Field>
          <Field label="Senha" htmlFor="password" error={form.formState.errors.password?.message}>
            <Input
              id="password"
              type="password"
              invalid={!!form.formState.errors.password}
              {...form.register("password")}
            />
            <p className="text-xs text-[var(--text-secondary)]">Força: {strength}</p>
          </Field>
          {registerMutation.isError ? (
            <Alert tone="danger">Não foi possível cadastrar. Tente novamente.</Alert>
          ) : null}
          <Button
            type="submit"
            className="w-full"
            disabled={registerMutation.isPending || cooldownSeconds > 0}
          >
            {registerMutation.isPending
              ? "Cadastrando..."
              : cooldownSeconds > 0
                ? `Tente novamente em ${cooldownSeconds}s`
                : "Cadastrar"}
          </Button>
        </form>
        <p className="mt-4 text-sm text-[var(--text-secondary)]">
          Já tem conta?{" "}
          <Link className="text-[var(--accent)]" href="/login">
            Entrar
          </Link>
        </p>
      </section>
    </main>
  );
}
