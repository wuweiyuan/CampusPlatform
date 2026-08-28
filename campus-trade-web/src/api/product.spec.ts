import { beforeEach, describe, expect, it, vi } from "vitest";

const http = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
}));

vi.mock("./http", () => ({ default: http }));

import {
  createProduct,
  getMyProducts,
  getProduct,
  getProducts,
  offShelfProduct,
  updateProduct,
} from "./product";

describe("product API", () => {
  beforeEach(() => vi.clearAllMocks());

  it("uses documented public product endpoints and list parameters", () => {
    getProducts({ page: 2, pageSize: 12, categoryId: 3, keyword: "教材" });
    getProduct(9);

    expect(http.get).toHaveBeenNthCalledWith(1, "/products", {
      params: { page: 2, pageSize: 12, categoryId: 3, keyword: "教材" },
    });
    expect(http.get).toHaveBeenNthCalledWith(2, "/products/9");
  });

  it("uses authenticated create, update, off-shelf and mine endpoints", () => {
    const payload = {
      categoryId: 1,
      title: "二手教材",
      description: "保存良好的高等数学教材。",
      price: 20,
      imageBase64: null,
    };
    createProduct(payload);
    updateProduct(9, payload);
    offShelfProduct(9);
    getMyProducts({ page: 1, pageSize: 12, status: "ON_SALE" });

    expect(http.post).toHaveBeenNthCalledWith(1, "/products", payload);
    expect(http.put).toHaveBeenCalledWith("/products/9", payload);
    expect(http.post).toHaveBeenNthCalledWith(2, "/products/9/off-shelf");
    expect(http.get).toHaveBeenCalledWith("/products/mine", {
      params: { page: 1, pageSize: 12, status: "ON_SALE" },
    });
  });
});
