import { AxiosError, type AxiosResponse } from "axios";
import { describe, expect, it } from "vitest";
import { getApiErrorMessage } from "./api-error";

function axiosErrorWith(message: string) {
  const response = { data: { message } } as unknown as AxiosResponse;
  return new AxiosError("request failed", "ERR_BAD_REQUEST", undefined, undefined, response);
}

describe("getApiErrorMessage", () => {
  it("translates a known backend message to Portuguese", () => {
    expect(getApiErrorMessage(axiosErrorWith("Email already registered"), "fallback")).toBe(
      "Este e-mail já está cadastrado.",
    );
  });

  it("falls back for an unknown backend message", () => {
    expect(getApiErrorMessage(axiosErrorWith("A brand new error"), "Tente novamente.")).toBe(
      "Tente novamente.",
    );
  });

  it("falls back for a non-axios error", () => {
    expect(getApiErrorMessage(new Error("boom"), "Padrão")).toBe("Padrão");
  });
});
