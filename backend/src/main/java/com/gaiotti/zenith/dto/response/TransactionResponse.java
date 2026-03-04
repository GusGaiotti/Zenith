package com.gaiotti.zenith.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private String type;
    private LocalDate date;
    private Long categoryId;
    private String categoryName;
    private String description;
    private Long createdByUserId;
    private String createdByDisplayName;
    private LocalDateTime createdAt;
}
