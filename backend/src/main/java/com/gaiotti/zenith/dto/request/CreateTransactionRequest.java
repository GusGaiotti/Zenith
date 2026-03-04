package com.gaiotti.zenith.dto.request;

import com.gaiotti.zenith.model.Transaction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateTransactionRequest {

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @NotNull
    private Transaction.TransactionType type;

    @NotNull
    private LocalDate date;

    private Long categoryId;

    private String description;
}
