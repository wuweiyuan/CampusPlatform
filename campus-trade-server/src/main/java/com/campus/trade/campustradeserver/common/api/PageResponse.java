package com.campus.trade.campustradeserver.common.api;


import lombok.Data;

import java.util.List;

@Data
public class PageResponse<T> {
    private Integer page;
    private Integer pageSize;
    private Long total;
    private List<T> records;
}
