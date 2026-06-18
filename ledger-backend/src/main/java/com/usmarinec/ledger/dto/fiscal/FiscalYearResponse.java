package com.usmarinec.ledger.dto.fiscal;

import com.usmarinec.ledger.dto.Response;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record FiscalYearResponse(
    @NotNull UUID id,
    @NotNull UUID accountingEntityId,
    @NotNull Integer year,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate)
    implements Response {}
