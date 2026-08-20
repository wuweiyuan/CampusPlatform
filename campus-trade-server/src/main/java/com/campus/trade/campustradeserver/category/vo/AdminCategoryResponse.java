package com.campus.trade.campustradeserver.category.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminCategoryResponse {

    private Long id;

    private String name;

    private Integer sort;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
