import { z } from "zod";

export const categorySchema = z.object({
  name: z.string().min(1, "Name is required"),
  color: z.string().regex(/^#[0-9A-Fa-f]{6}$/, "Color must be a valid hex code"),
});

export type CategorySchema = z.infer<typeof categorySchema>;
