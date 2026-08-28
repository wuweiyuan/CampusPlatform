import { describe, expect, it } from "vitest";
import { validateImageFile, validatePrice } from "./product";

describe("validatePrice", () => {
  it("accepts a positive price with at most two decimal places", () => {
    expect(validatePrice("25.50")).toBe("");
  });

  it("rejects zero and prices with more than two decimal places", () => {
    expect(validatePrice("0")).toBe("价格必须大于 0");
    expect(validatePrice("1.234")).toBe("价格最多保留两位小数");
  });
});

describe("validateImageFile", () => {
  it("rejects an unsupported image type and a file over 2 MB", () => {
    expect(
      validateImageFile(new File(["x"], "a.gif", { type: "image/gif" })),
    ).toBe("仅支持 JPEG、PNG 或 WebP 图片");
    expect(
      validateImageFile(
        new File([new Uint8Array(2 * 1024 * 1024 + 1)], "a.png", {
          type: "image/png",
        }),
      ),
    ).toBe("图片大小不能超过 2 MB");
  });
});
