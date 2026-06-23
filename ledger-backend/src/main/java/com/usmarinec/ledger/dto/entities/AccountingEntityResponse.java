package com.usmarinec.ledger.dto.entities;

import com.usmarinec.ledger.dto.DomainResponse;
import com.usmarinec.ledger.dto.Response;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(name = "AccountingEntityResponse", description = "Accounting entity returned by the API.")
public record AccountingEntityResponse(
    @Schema(
            description = "Unique identifier of the accounting entity.",
            example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
    @Schema(
            description = "Display name of the accounting entity.",
            example = "Nolan Consulting LLC")
        String name,
    @Schema(
            description = "Date and time when the accounting entity was created.",
            example = "2026-06-20T14:30:00")
        LocalDateTime createdAt)
    implements Response, DomainResponse {}
