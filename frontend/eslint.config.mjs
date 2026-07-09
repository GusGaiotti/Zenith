import { defineConfig, globalIgnores } from "eslint/config";
import nextVitals from "eslint-config-next/core-web-vitals";
import nextTs from "eslint-config-next/typescript";
import jsxA11y from "eslint-plugin-jsx-a11y";

const eslintConfig = defineConfig([
  ...nextVitals,
  ...nextTs,
  {
    rules: {
      ...jsxA11y.flatConfigs.recommended.rules,
      // Labels estilizados aninham o texto em spans; a profundidade padrao (2) nao alcanca.
      "jsx-a11y/label-has-associated-control": ["error", { depth: 3 }],
      // Containers que fecham ao clicar fora sao layout, nao widget: o fechamento
      // acessivel vem do botao Fechar e da tecla Esc. Fica como aviso, nao erro.
      "jsx-a11y/no-static-element-interactions": "warn",
      "no-restricted-syntax": [
        "warn",
        {
          selector:
            "Literal[value=/\\b(?:bg|text|border|ring|outline|from|via|to|fill|stroke|divide|placeholder)-(?:red|orange|amber|yellow|lime|green|emerald|teal|cyan|sky|blue|indigo|violet|purple|fuchsia|pink|rose|slate|gray|zinc|neutral|stone)-\\d{2,3}\\b/]",
          message:
            "Cor de paleta Tailwind crua. Use um token do design system (var(--...)) ou um primitivo de ui/.",
        },
      ],
    },
  },
  // Override default ignores of eslint-config-next.
  globalIgnores([
    // Default ignores of eslint-config-next:
    ".next/**",
    "out/**",
    "build/**",
    "next-env.d.ts",
  ]),
]);

export default eslintConfig;
