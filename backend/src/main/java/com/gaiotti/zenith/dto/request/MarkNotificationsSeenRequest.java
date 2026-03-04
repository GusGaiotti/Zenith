package com.gaiotti.zenith.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class MarkNotificationsSeenRequest {
    @NotEmpty(message = "At least one notification id is required")
    private List<Long> ids;
}
