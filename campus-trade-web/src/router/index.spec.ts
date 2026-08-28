import { describe, expect, it, vi } from "vitest";

vi.mock("../layouts/AppLayout.vue", () => ({ default: {} }));

import router from "./index";

describe("product route access", () => {
  it("keeps browsing public and protects product management", () => {
    expect(router.resolve("/").meta.requiresAuth).not.toBe(true);
    expect(router.resolve("/products/1").meta.requiresAuth).not.toBe(true);
    expect(router.resolve("/products/new").meta.requiresAuth).toBe(true);
    expect(router.resolve("/products/1/edit").meta.requiresAuth).toBe(true);
    expect(router.resolve("/my-products").meta.requiresAuth).toBe(true);
    expect(router.resolve("/favorites").meta.requiresAuth).toBe(true);
  });
});
