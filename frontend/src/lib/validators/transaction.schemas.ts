import { z } from "zod";

export const transactionSchema = z.object({
  amount: z.number().positive("Amount must be greater than 0"),
  type: z.enum(["INCOME", "EXPENSE"]),
  date: z.string().min(1, "Date is required"),
  categoryId: z.number().nullable().optional(),
  description: z.string().max(255).optional().nullable(),
});

export type TransactionSchema = z.infer<typeof transactionSchema>;
