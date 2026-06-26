package com.usmarinec.ledger.dto.fiscal;

import com.usmarinec.ledger.dto.UpdateRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "UpdateFiscalYearRequest", description = "Request body for updating a fiscal year.")
public record UpdateFiscalYearRequest(
    @Schema(
            description = "UUID of the accounting entity.",
            example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull
        UUID accountingEntityId,
    @Schema(description = "Fiscal year label.", example = "2026") @NotNull Integer year,
    @Schema(description = "First date of the fiscal year.", example = "2026-01-01") @NotNull
        LocalDate startDate,
    @Schema(description = "Last date of the fiscal year.", example = "2026-12-31") @NotNull
        LocalDate endDate,
    @Schema(description = "Whether this fiscal year is closed.", example = "false") boolean closed)
    implements UpdateRequest {}
