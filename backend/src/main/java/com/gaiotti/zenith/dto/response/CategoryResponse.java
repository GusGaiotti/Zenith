package com.gaiotti.zenith.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String color;
    private LocalDateTime createdAt;
    private Long createdByUserId;
    private String createdByDisplayName;
}
