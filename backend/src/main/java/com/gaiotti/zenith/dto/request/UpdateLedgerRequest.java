package com.gaiotti.zenith.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateLedgerRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 120, message = "Name must have at most 120 characters")
    private String name;
}
