package com.campus.trade.campustradeserver.common.api;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.util.List;

@Data
@Schema(description = "分页结果")
public class PageResponse<T> {
    @Schema(description = "当前页码，从 1 开始")
    private Integer page;
    @Schema(description = "本次查询的每页数量")
    private Integer pageSize;
    @Schema(description = "符合条件的记录总数，不是总页数")
    private Long total;
    @Schema(description = "当前页数据列表")
    private List<T> records;
}
