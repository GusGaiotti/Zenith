import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { Alert } from "./Alert";

describe("Alert", () => {
  it("has a status role and renders its message", () => {
    render(<Alert>Não foi possível salvar.</Alert>);
    expect(screen.getByRole("status")).toHaveTextContent("Não foi possível salvar.");
  });

  it("applies the success tone", () => {
    render(<Alert tone="success">Salvo com sucesso.</Alert>);
    expect(screen.getByRole("status").className).toContain("var(--income)");
  });
});
