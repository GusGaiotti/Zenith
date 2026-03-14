"use client";

import { AxiosError } from "axios";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { BrandWordmark } from "@/components/brand/BrandWordmark";
import { ThemeToggle } from "@/components/theme/ThemeToggle";
import { useLogin } from "@/hooks/useAuth";
import { getMyLedger } from "@/lib/api/ledger";
import { useAuthStore } from "@/lib/store/auth.store";
import { loginSchema, type LoginSchema } from "@/lib/validators/auth.schemas";

function LockIcon() {
  return (
    <svg aria-hidden className="h-3.5 w-3.5" fill="none" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.9" viewBox="0 0 24 24">
      <rect x="5" y="11" width="14" height="9" rx="2.5" />
      <path d="M8 11V8.5a4 4 0 1 1 8 0V11" />
    </svg>
  );
}

export default function LoginPage() {
  const router = useRouter();
  const loginMutation = useLogin();
  const setActiveLedger = useAuthStore((state) => state.setActiveLedger);
  const [cooldownSeconds, setCooldownSeconds] = useState(0);
  const [isPreparingRoute, setIsPreparingRoute] = useState(false);

  useEffect(() => {
    if (cooldownSeconds <= 0) return;
    const timer = setInterval(() => {
      setCooldownSeconds((value) => Math.max(0, value - 1));
    }, 1000);
    return () => clearInterval(timer);
  }, [cooldownSeconds]);

  const form = useForm<LoginSchema>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "", password: "" },
  });

  const onSubmit = form.handleSubmit((values) => {
    setIsPreparingRoute(false);
    loginMutation.mutate(values, {
      onSuccess: async () => {
        setCooldownSeconds(0);
        setIsPreparingRoute(true);
        try {
          await new Promise((resolve) => setTimeout(resolve, 900));
          const response = await getMyLedger();
          setActiveLedger(response.data.id);
          router.push("/dashboard");
        } catch (error) {
          const axiosError = error as AxiosError;
          if (axiosError.response?.status === 404) {
            setActiveLedger(null);
            router.push("/onboarding");
            return;
          }
          router.push("/dashboard");
        }
      },
      onError: () => {
        setIsPreparingRoute(false);
        setCooldownSeconds(5);
      },
    });
  });

  return (
    <main className="relative flex min-h-screen items-center justify-center overflow-hidden p-4 sm:p-6">
      <div className="absolute right-4 top-4 z-10">
        <div className="w-[190px]">
          <ThemeToggle />
        </div>
      </div>
      <section className="surface relative w-full max-w-5xl overflow-hidden p-0">
        <div className="grid min-h-[620px] lg:grid-cols-[1.1fr_0.9fr]">
          <div className="relative hidden flex-col justify-between border-r border-[var(--surface-edge)] bg-[linear-gradient(145deg,color-mix(in_srgb,var(--accent)_22%,transparent),transparent_52%),radial-gradient(circle_at_20%_18%,color-mix(in_srgb,var(--accent-violet)_16%,transparent),transparent_36%),linear-gradient(180deg,var(--surface-gradient-start),var(--elevated-gradient-end))] p-10 lg:flex">
            <div>
              <BrandWordmark animate labelClassName="text-6xl text-[var(--text-primary)]" />
              <h1 className="mt-6 max-w-sm text-4xl font-semibold leading-tight text-[var(--text-primary)]">
                Controle financeiro compartilhado com clareza, ritmo e tranquilidade.
              </h1>
            </div>
            <ul className="space-y-3 text-sm text-[var(--text-secondary)]">
              <li className="rounded-xl border border-[color-mix(in_srgb,var(--accent)_18%,transparent)] bg-[color-mix(in_srgb,var(--accent)_10%,var(--panel-bg))] px-4 py-3 shadow-[var(--elevated-shadow)]">Saldo consolidado do casal em tempo real.</li>
              <li className="rounded-xl border border-[color-mix(in_srgb,var(--accent-emerald)_18%,transparent)] bg-[color-mix(in_srgb,var(--accent-emerald)_10%,var(--panel-bg))] px-4 py-3 shadow-[var(--elevated-shadow)]">Transações organizadas por pessoa e categoria.</li>
              <li className="rounded-xl border border-[color-mix(in_srgb,var(--accent-amber)_18%,transparent)] bg-[color-mix(in_srgb,var(--accent-amber)_10%,var(--panel-bg))] px-4 py-3 shadow-[var(--elevated-shadow)]">Visões rápidas para o fechamento do mês.</li>
            </ul>
          </div>

          <div className="bg-[linear-gradient(180deg,color-mix(in_srgb,var(--bg-surface)_58%,var(--accent)_2%),color-mix(in_srgb,var(--panel-bg)_84%,var(--accent-amber)_3%))] p-6 sm:p-10">
            <div className="mx-auto w-full max-w-md">
              <div className="inline-flex items-center gap-2 rounded-full border border-[color-mix(in_srgb,var(--income)_26%,transparent)] bg-[color-mix(in_srgb,var(--income)_10%,transparent)] px-3 py-1 text-xs font-semibold uppercase tracking-[0.12em] text-[var(--income)]">
                <LockIcon />
                <span>Acesso seguro</span>
              </div>
              <h2 className="mt-3 font-display text-3xl leading-tight text-[var(--text-primary)]">Entrar na sua conta</h2>
              <p className="mt-2 text-sm text-[var(--text-secondary)]">Use seu email e senha para continuar.</p>

              <form className="mt-8 space-y-5" onSubmit={onSubmit}>
                <label className="block text-sm">
                  <span className="mb-1.5 block font-medium text-[var(--text-secondary)]">Email</span>
                  <input
                    type="email"
                    className="focusable h-11 w-full rounded-xl border border-[var(--border)] bg-[var(--bg-elevated)] px-3.5 outline-none transition-colors duration-150"
                    {...form.register("email")}
                  />
                  {form.formState.errors.email ? (
                    <span className="mt-1.5 block text-xs text-red-300">{form.formState.errors.email.message}</span>
                  ) : null}
                </label>
                <label className="block text-sm">
                  <span className="mb-1.5 block font-medium text-[var(--text-secondary)]">Senha</span>
                  <input
                    type="password"
                    className="focusable h-11 w-full rounded-xl border border-[var(--border)] bg-[var(--bg-elevated)] px-3.5 outline-none transition-colors duration-150"
                    {...form.register("password")}
                  />
                  {form.formState.errors.password ? (
                    <span className="mt-1.5 block text-xs text-red-300">{form.formState.errors.password.message}</span>
                  ) : null}
                </label>
                {loginMutation.isError ? (
                  <p className="danger-chip rounded-xl px-3 py-2.5 text-sm">
                    Credenciais inválidas ou servidor indisponível.
                  </p>
                ) : null}
                <button
                  disabled={loginMutation.isPending || isPreparingRoute || cooldownSeconds > 0}
                  className="focusable h-11 w-full rounded-xl bg-[var(--accent)] px-4 font-semibold text-white shadow-[0_8px_24px_rgba(79,124,255,0.35)] transition-all duration-150 hover:bg-[var(--accent-hover)] disabled:cursor-not-allowed disabled:opacity-45"
                >
                  {loginMutation.isPending
                    ? "Entrando..."
                    : isPreparingRoute
                      ? "Abrindo seu painel..."
                    : cooldownSeconds > 0
                      ? `Tente novamente em ${cooldownSeconds}s`
                      : "Entrar"}
                </button>
              </form>
              <p className="mt-5 text-sm text-[var(--text-secondary)]">
                Não tem conta?{" "}
                <Link className="font-semibold text-[var(--accent-hover)]" href="/register">
                  Cadastre-se
                </Link>
              </p>
            </div>
          </div>
        </div>
      </section>
    </main>
  );
}
