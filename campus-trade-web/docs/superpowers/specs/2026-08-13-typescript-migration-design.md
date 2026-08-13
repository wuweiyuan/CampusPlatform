# Strict TypeScript Migration Design

## Scope

Convert the Vue/Vite application source and project tooling to standard strict TypeScript while preserving the existing health-check route and its behavior. Do not add Vitest, jsdom, or `@vue/test-utils`.

## Tooling

Install `typescript` and `vue-tsc` as development dependencies. Add a strict `tsconfig.json` appropriate for Vite and Vue single-file components, plus `src/env.d.ts` for Vite client types. Change the build script to run `vue-tsc --noEmit` before `vite build`.

## Source Migration

Rename `src/main.js`, `src/api/http.js`, and `src/router/index.js` to `.ts`. Add `lang="ts"` to the health view script. The Axios call uses an explicit response type, while its response renderer safely handles the JSON values returned by the health endpoint. `App.vue` remains template-only.

## Verification

Run strict type checking directly and through the production build. Confirm the final dependency declarations contain the TypeScript tooling and still omit the requested test packages.
