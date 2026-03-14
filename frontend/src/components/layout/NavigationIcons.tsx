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

const iconClassName = "h-[18px] w-[18px]";

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
      strokeWidth="1.85"
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
          <path d="M5 6.5A1.5 1.5 0 0 1 6.5 5h3A1.5 1.5 0 0 1 11 6.5v3A1.5 1.5 0 0 1 9.5 11h-3A1.5 1.5 0 0 1 5 9.5v-3Z" />
          <path d="M13 6.5A1.5 1.5 0 0 1 14.5 5h3A1.5 1.5 0 0 1 19 6.5v6A1.5 1.5 0 0 1 17.5 14h-3A1.5 1.5 0 0 1 13 12.5v-6Z" />
          <path d="M5 14.5A1.5 1.5 0 0 1 6.5 13h3a1.5 1.5 0 0 1 1.5 1.5v3A1.5 1.5 0 0 1 9.5 19h-3A1.5 1.5 0 0 1 5 17.5v-3Z" />
          <path d="M13 17a2 2 0 0 1 2-2h2" />
          <path d="M13 17a2 2 0 0 0 2 2h2" />
        </IconBase>
      );
    case "transactions":
      return (
        <IconBase className={className}>
          <path d="M6 7.5h10" />
          <path d="m13.5 5 2.5 2.5-2.5 2.5" />
          <path d="M18 16.5H8" />
          <path d="m10.5 14-2.5 2.5 2.5 2.5" />
        </IconBase>
      );
    case "categories":
      return (
        <IconBase className={className}>
          <path d="M6 8.2 12 5l6 3.2-6 3.3-6-3.3Z" />
          <path d="M6 12.1 12 15.4l6-3.3" />
          <path d="M6 15.9 12 19l6-3.1" />
        </IconBase>
      );
    case "ledger":
      return (
        <IconBase className={className}>
          <rect x="5" y="4.8" width="14" height="14.5" rx="2.6" />
          <path d="M8.5 8.5h7" />
          <path d="M8.5 12.2h7" />
          <path d="M8.5 15.9h4" />
        </IconBase>
      );
    case "ask-ai":
      return (
        <IconBase className={className}>
          <path d="M12 5c4.4 0 8 2.7 8 6.2 0 1.9-1 3.6-2.7 4.8l.4 3-2.8-1.5a10.7 10.7 0 0 1-2.9.4c-4.4 0-8-2.7-8-6.2S7.6 5 12 5Z" />
          <path d="M8.7 10.5h6.6" />
          <path d="M10.1 13.5h3.8" />
        </IconBase>
      );
    case "lock":
      return (
        <IconBase className={className}>
          <rect x="5.5" y="10.8" width="13" height="8.2" rx="2.2" />
          <path d="M8.5 10.8V8.7a3.5 3.5 0 1 1 7 0v2.1" />
        </IconBase>
      );
    case "collapse":
      return (
        <IconBase className={className}>
          <path d="M7 5v14" />
          <path d="m16 7.4-4 4.6 4 4.6" />
        </IconBase>
      );
    case "expand":
      return (
        <IconBase className={className}>
          <path d="M17 5v14" />
          <path d="m8 7.4 4 4.6-4 4.6" />
        </IconBase>
      );
  }
}
