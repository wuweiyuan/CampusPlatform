import { describe, expect, it } from "vitest";
import market from "../market/ProductMarketView.vue?raw";
import mine from "./MyProductsView.vue?raw";

describe("product list page sizes", () => {
  it("defaults both lists to ten and provides the supported size options", () => {
    for (const source of [market, mine]) {
      expect(source).toContain("const pageSize = ref(10)");
      expect(source).toContain(':page-sizes="[10, 20, 30, 50]"');
      expect(source).toContain('@size-change="changePageSize"');
      expect(source).toContain('v-if="total > 0"');
    }
  });
});
