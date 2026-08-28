import { beforeEach, describe, expect, it, vi } from "vitest";

const http = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn(),
  delete: vi.fn(),
}));

vi.mock("./http", () => ({ default: http }));

import { addFavorite, getFavorites, removeFavorite } from "./favorite";

describe("favorite API", () => {
  beforeEach(() => vi.clearAllMocks());

  it("uses the documented favorite endpoints", () => {
    addFavorite(9);
    removeFavorite(9);
    getFavorites({ page: 2, pageSize: 12 });

    expect(http.post).toHaveBeenCalledWith("/products/9/favorite");
    expect(http.delete).toHaveBeenCalledWith("/products/9/favorite");
    expect(http.get).toHaveBeenCalledWith("/favorites", {
      params: { page: 2, pageSize: 12 },
    });
  });
});
