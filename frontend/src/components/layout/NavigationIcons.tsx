import type { SVGProps } from "react";

type IconName =
  | "dashboard"
  | "transactions"
  | "categories"
  | "ledger"
  | "ask-ai"
  | "lock"
  | "collapse"
  | "expand";

type NavItem = {
  href: string;
  label: string;
  icon: IconName;
};

const iconClassName = "h-4 w-4";

export const APP_NAV_ITEMS: NavItem[] = [
  { href: "/dashboard", label: "Dashboard", icon: "dashboard" },
  { href: "/transactions", label: "Transacoes", icon: "transactions" },
  { href: "/categories", label: "Categorias", icon: "categories" },
  { href: "/ledger", label: "Fatura", icon: "ledger" },
  { href: "/ask-ai", label: "Pergunte a IA", icon: "ask-ai" },
];

function IconBase(props: SVGProps<SVGSVGElement>) {
  return (
    <svg
      aria-hidden
      fill="none"
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth="1.8"
      viewBox="0 0 24 24"
      {...props}
    />
  );
}

export function NavigationIcon({ name, className = iconClassName }: { name: IconName; className?: string }) {
  switch (name) {
    case "dashboard":
      return (
        <IconBase className={className}>
          <rect x="4" y="4.5" width="7" height="7" rx="2" />
          <rect x="13" y="4.5" width="7" height="10" rx="2" />
          <rect x="4" y="13.5" width="7" height="6" rx="2" />
          <rect x="13" y="16.5" width="7" height="3" rx="1.5" />
        </IconBase>
      );
    case "transactions":
      return (
        <IconBase className={className}>
          <path d="M6 8h11" />
          <path d="m14 5 3 3-3 3" />
          <path d="M18 16H7" />
          <path d="m10 13-3 3 3 3" />
        </IconBase>
      );
    case "categories":
      return (
        <IconBase className={className}>
          <path d="M12 4.5 4.5 8.7 12 13l7.5-4.3L12 4.5Z" />
          <path d="M4.5 15.3 12 19.5l7.5-4.2" />
          <path d="M4.5 12 12 16.2l7.5-4.2" />
        </IconBase>
      );
    case "ledger":
      return (
        <IconBase className={className}>
          <rect x="4" y="5" width="16" height="14" rx="3" />
          <path d="M4 10.5h16" />
          <path d="M8 15h3.5" />
          <path d="M14 15h2.5" />
        </IconBase>
      );
    case "ask-ai":
      return (
        <IconBase className={className}>
          <path d="M12 4c4.8 0 8.5 2.95 8.5 6.9 0 2.17-1.1 4.03-2.98 5.31L18 20l-3.65-1.55c-.73.13-1.5.2-2.35.2-4.8 0-8.5-2.95-8.5-6.9S7.2 4 12 4Z" />
          <path d="M9.8 10.1c.23-1.12 1.14-1.9 2.2-1.9 1.25 0 2.2.85 2.2 2 0 1-.55 1.53-1.35 2.08-.64.43-.98.72-.98 1.42" />
          <path d="M12 15.8h.01" />
        </IconBase>
      );
    case "lock":
      return (
        <IconBase className={className}>
          <rect x="5.5" y="11" width="13" height="8" rx="2.2" />
          <path d="M8.5 11V8.7a3.5 3.5 0 1 1 7 0V11" />
        </IconBase>
      );
    case "collapse":
      return (
        <IconBase className={className}>
          <path d="M8 5v14" />
          <path d="m16 7-4 5 4 5" />
        </IconBase>
      );
    case "expand":
      return (
        <IconBase className={className}>
          <path d="M16 5v14" />
          <path d="m8 7 4 5-4 5" />
        </IconBase>
      );
  }
}
