export function requireLedgerId(ledgerId: number | null): number {
  if (!ledgerId) {
    throw new Error("Active ledger is not set.");
  }

  return ledgerId;
}
