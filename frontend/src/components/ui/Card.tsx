import { cva, type VariantProps } from "class-variance-authority";
import type { HTMLAttributes } from "react";
import { cn } from "@/lib/utils/cn";

const cardVariants = cva("", {
  variants: {
    tone: {
      surface: "surface",
      elevated: "elevated",
    },
    padding: {
      none: "",
      sm: "p-4",
      md: "p-6",
      lg: "p-8",
    },
  },
  defaultVariants: {
    tone: "surface",
    padding: "md",
  },
});

type CardProps = HTMLAttributes<HTMLDivElement> & VariantProps<typeof cardVariants>;

export function Card({ className, tone, padding, ...props }: CardProps) {
  return <div className={cn(cardVariants({ tone, padding }), className)} {...props} />;
}
