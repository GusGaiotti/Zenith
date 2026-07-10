import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { Field } from "./Field";

describe("Field", () => {
  it("associates the label with its control via htmlFor", () => {
    render(
      <Field label="Email" htmlFor="email">
        <input id="email" />
      </Field>,
    );
    expect(screen.getByLabelText("Email")).toBeInTheDocument();
  });

  it("shows the error message", () => {
    render(
      <Field label="Senha" error="Obrigatória">
        <input />
      </Field>,
    );
    expect(screen.getByText("Obrigatória")).toBeInTheDocument();
  });

  it("hides the hint when there is an error", () => {
    render(
      <Field label="Senha" hint="Mínimo 6 caracteres" error="Muito curta">
        <input />
      </Field>,
    );
    expect(screen.queryByText("Mínimo 6 caracteres")).not.toBeInTheDocument();
    expect(screen.getByText("Muito curta")).toBeInTheDocument();
  });

  it("renders the action slot", () => {
    render(
      <Field label="Senha" action={<a href="/reset">Esqueci</a>}>
        <input />
      </Field>,
    );
    expect(screen.getByRole("link", { name: "Esqueci" })).toBeInTheDocument();
  });
});
