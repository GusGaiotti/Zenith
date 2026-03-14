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
  { href: "/transactions", label: "Transações", icon: "transactions" },
  { href: "/categories", label: "Categorias", icon: "categories" },
  { href: "/ledger", label: "Fatura", icon: "ledger" },
  { href: "/ask-ai", label: "Pergunte à IA", icon: "ask-ai" },
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
          <path d="M4.5 7.5A3 3 0 0 1 7.5 4.5h3a3 3 0 0 1 3 3v3a3 3 0 0 1-3 3h-3a3 3 0 0 1-3-3v-3Z" />
          <path d="M15 7.5a3 3 0 0 1 3-3h.5a2.5 2.5 0 0 1 2.5 2.5V13a2.5 2.5 0 0 1-2.5 2.5H18a3 3 0 0 1-3-3V7.5Z" />
          <path d="M4.5 17.5a2.5 2.5 0 0 1 2.5-2.5H11a2.5 2.5 0 0 1 2.5 2.5v.5A2.5 2.5 0 0 1 11 20.5H7A2.5 2.5 0 0 1 4.5 18v-.5Z" />
          <path d="M15 18a2.5 2.5 0 0 1 2.5-2.5h1A2.5 2.5 0 0 1 21 18a2.5 2.5 0 0 1-2.5 2.5h-1A2.5 2.5 0 0 1 15 18Z" />
        </IconBase>
      );
    case "transactions":
      return (
        <IconBase className={className}>
          <path d="M6 7.5h8.5" />
          <path d="m12.5 4.8 2.7 2.7-2.7 2.7" />
          <path d="M18 16.5H9.5" />
          <path d="m11.5 13.8-2.7 2.7 2.7 2.7" />
          <path d="M4.5 5.5v13" />
          <path d="M19.5 5.5v13" />
        </IconBase>
      );
    case "categories":
      return (
        <IconBase className={className}>
          <path d="M12 4.5 5 8.6 12 12.7l7-4.1L12 4.5Z" />
          <path d="M5 12.2 12 16.3l7-4.1" />
          <path d="M5 15.9 12 20l7-4.1" />
        </IconBase>
      );
    case "ledger":
      return (
        <IconBase className={className}>
          <rect x="4.5" y="5" width="15" height="14" rx="3" />
          <path d="M8 9h8" />
          <path d="M8 13h8" />
          <path d="M8 17h4.5" />
          <path d="M15.7 5v14" />
        </IconBase>
      );
    case "ask-ai":
      return (
        <IconBase className={className}>
          <path d="M12 4.5c4.6 0 8.2 2.9 8.2 6.6 0 2-1 3.8-2.8 5l.3 3.4-3.1-1.6a11 11 0 0 1-2.6.3c-4.6 0-8.2-2.9-8.2-6.6S7.4 4.5 12 4.5Z" />
          <path d="M9 10.5h6" />
          <path d="M9.8 13.5h4.4" />
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
          <path d="M6.5 5v14" />
          <path d="m16 7.2-4 4.8 4 4.8" />
        </IconBase>
      );
    case "expand":
      return (
        <IconBase className={className}>
          <path d="M17.5 5v14" />
          <path d="m8 7.2 4 4.8-4 4.8" />
        </IconBase>
      );
  }
}
