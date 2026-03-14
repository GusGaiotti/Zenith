package com.gaiotti.zenith.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AskAiRequest {

    public static final int MAX_QUESTION_LENGTH = 300;

    @NotBlank
    @Size(max = MAX_QUESTION_LENGTH)
    private String question;

    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "yearMonth must match yyyy-MM")
    private String yearMonth;

    private Boolean includeTransactions = false;
}
