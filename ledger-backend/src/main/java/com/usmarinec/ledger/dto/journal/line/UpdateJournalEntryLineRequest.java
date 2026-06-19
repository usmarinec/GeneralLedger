package com.usmarinec.ledger.dto.journal.line;

import com.usmarinec.ledger.dto.UpdateRequest;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record UpdateJournalEntryLineRequest(
    @NotNull UUID accountId,
    @Size(max = 1000) String description,
    @DecimalMin("0.00") BigDecimal debitAmount,
    @DecimalMin("0.00") BigDecimal creditAmount)
    implements UpdateRequest {}
