import { AxiosError } from "axios";

const KNOWN_API_MESSAGES: Record<string, string> = {
  "Invalid email or password": "E-mail ou senha inválidos.",
  "Email already registered": "Este e-mail já está cadastrado.",
  "Current password is incorrect": "A senha atual está incorreta.",
  "Current password is required to set a new password":
    "Informe a senha atual para definir uma nova senha.",
  "User already belongs to a ledger": "Você já participa de uma fatura.",
  "User already belongs to another ledger": "Essa pessoa já participa de outra fatura.",
  "User is already a member of this ledger": "Essa pessoa já participa desta fatura.",
  "No registered user with that email": "Não encontramos uma conta com esse e-mail.",
  "A pending invitation already exists for that email":
    "Já existe um convite pendente para esse e-mail.",
  "Ledger already has the maximum of 2 members": "A fatura já tem o limite de 2 participantes.",
  "Invitation is not pending": "Esse convite não está mais pendente.",
  "Invitation has expired": "Esse convite expirou.",
  "You are not the intended recipient of this invitation":
    "Esse convite foi enviado para outra pessoa.",
  "Only the inviter can cancel this invitation": "Somente quem enviou o convite pode cancelá-lo.",
  "You are not a member of this ledger": "Você não participa desta fatura.",
  "Cannot delete category: it has associated transactions":
    "Não é possível excluir a categoria porque ela possui transações associadas. Exclua as transações vinculadas antes de tentar novamente.",
  "Category does not belong to this ledger": "Essa categoria não pertence a esta fatura.",
  "Ledger name is required": "Informe um nome para a fatura.",
  "Ledger name must have at most 120 characters":
    "O nome da fatura pode ter no máximo 120 caracteres.",
  "Too many authentication attempts. Please try again shortly.":
    "Muitas tentativas em sequência. Aguarde um instante e tente novamente.",
  "Invalid or expired reset token": "Link de redefinição inválido ou expirado.",
  "Reset token has already been used": "Esse link de redefinição já foi usado.",
  "Reset token has expired": "Esse link de redefinição expirou.",
  "AI rate limit exceeded for this user":
    "Limite de perguntas por minuto atingido. Aguarde um instante.",
  "AI rate limit exceeded for this IP":
    "Limite de perguntas por minuto atingido. Aguarde um instante.",
  "Daily AI quota exceeded for this user":
    "Cota diária do assistente atingida. Tente novamente amanhã.",
};

export function getApiErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof AxiosError) {
    const message = (error.response?.data as { message?: string } | undefined)?.message;
    if (message && KNOWN_API_MESSAGES[message]) {
      return KNOWN_API_MESSAGES[message];
    }
  }
  return fallback;
}
