import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { Badge } from "./Badge";

describe("Badge", () => {
  it("renders its content", () => {
    render(<Badge>Ana</Badge>);
    expect(screen.getByText("Ana")).toBeInTheDocument();
  });

  it("applies the expense tone", () => {
    render(<Badge tone="expense">R$ 100</Badge>);
    expect(screen.getByText("R$ 100").className).toContain("var(--danger-text)");
  });
});
