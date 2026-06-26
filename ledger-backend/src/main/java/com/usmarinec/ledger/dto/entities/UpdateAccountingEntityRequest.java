package com.usmarinec.ledger.dto.entities;

import com.usmarinec.ledger.dto.UpdateRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
    name = "UpdateAccountingEntityRequest",
    description = "Request body for updating an accounting entity.")
public record UpdateAccountingEntityRequest(
    @Schema(
            description = "Updated display name of the accounting entity.",
            example = "Nolan Consulting LLC",
            maxLength = 255)
        @NotBlank
        @Size(max = 255)
        String name)
    implements UpdateRequest {}
