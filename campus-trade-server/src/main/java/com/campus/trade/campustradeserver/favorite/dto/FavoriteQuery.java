package com.campus.trade.campustradeserver.favorite.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class FavoriteQuery {
    @Min(value = 1,message = "页码最小为1")
    private Integer page = 1;

    @Min(value = 1, message = "每页数量最小为 1")
    @Max(value = 50, message = "每页数量最大为 50")
    private Integer pageSize = 12;
}
