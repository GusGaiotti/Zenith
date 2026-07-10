import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { Input } from "./Input";

describe("Input", () => {
  it("forwards native props", () => {
    render(<Input placeholder="Nome" defaultValue="Ana" />);
    expect(screen.getByPlaceholderText("Nome")).toHaveValue("Ana");
  });

  it("uses the neutral border by default", () => {
    render(<Input placeholder="neutro" />);
    expect(screen.getByPlaceholderText("neutro").className).toContain("var(--border)");
  });

  it("uses the danger border when invalid", () => {
    render(<Input invalid placeholder="erro" />);
    expect(screen.getByPlaceholderText("erro").className).toContain("var(--danger-border)");
  });
});
