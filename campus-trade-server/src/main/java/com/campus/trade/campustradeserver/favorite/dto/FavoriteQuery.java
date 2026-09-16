package com.campus.trade.campustradeserver.favorite.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "收藏分页查询条件")
public class FavoriteQuery {
    @Min(value = 1,message = "页码最小为1")
    @Schema(description = "页码，从 1 开始，默认 1", defaultValue = "1")
    private Integer page = 1;

    @Min(value = 1, message = "每页数量最小为 1")
    @Max(value = 50, message = "每页数量最大为 50")
    @Schema(description = "每页数量，范围 1～50，默认 12", defaultValue = "12")
    private Integer pageSize = 12;
}
