package com.usmarinec.ledger.dto.fiscal;

import com.usmarinec.ledger.dto.UpdateRequest;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateFiscalYearRequest(
    @NotNull UUID accountingEntityId,
    @NotNull Integer year,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    @NotNull boolean closed)
    implements UpdateRequest {}
