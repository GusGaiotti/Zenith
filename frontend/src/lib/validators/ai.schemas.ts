import { z } from "zod";
import { ASK_AI_MAX_QUESTION_LENGTH } from "@/lib/constants/ai";

export const askAiSchema = z.object({
  question: z
    .string()
    .trim()
    .min(5, "Pergunta muito curta")
    .max(ASK_AI_MAX_QUESTION_LENGTH, `Pergunta deve ter no maximo ${ASK_AI_MAX_QUESTION_LENGTH} caracteres`),
  yearMonth: z.string().regex(/^\d{4}-\d{2}$/, "Mes invalido"),
  includeTransactions: z.boolean(),
});

export type AskAiSchema = z.infer<typeof askAiSchema>;
