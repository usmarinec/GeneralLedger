package com.usmarinec.ledger.dto.entities;

import com.usmarinec.ledger.dto.CreateRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
    name = "CreateAccountingEntityRequest",
    description = "Request body for creating a new accounting entity.")
public record CreateAccountingEntityRequest(
    @Schema(
            description = "Display name of the accounting entity.",
            example = "Nolan Consulting LLC",
            maxLength = 255)
        @NotBlank
        @Size(max = 255)
        String name)
    implements CreateRequest {}
