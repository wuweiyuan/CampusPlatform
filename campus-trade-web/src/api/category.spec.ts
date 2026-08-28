import { expect, it, vi } from "vitest";

const http = vi.hoisted(() => ({ get: vi.fn() }));
vi.mock("./http", () => ({ default: http }));

import { getCategories } from "./category";

it("loads enabled categories from the public endpoint", () => {
  getCategories();
  expect(http.get).toHaveBeenCalledWith("/categories");
});
