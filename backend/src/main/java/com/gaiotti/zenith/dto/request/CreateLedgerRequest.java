package com.gaiotti.zenith.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateLedgerRequest {
    @NotBlank(message = "Name is required")
    private String name;
}
