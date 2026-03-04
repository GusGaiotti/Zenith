package com.gaiotti.zenith.dto.request;

import com.gaiotti.zenith.model.Transaction;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateTransactionRequest {

    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    private Transaction.TransactionType type;

    private LocalDate date;

    private Long categoryId;

    private String description;
}
