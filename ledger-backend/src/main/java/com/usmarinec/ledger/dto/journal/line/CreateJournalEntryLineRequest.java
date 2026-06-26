package com.usmarinec.ledger.dto.journal.line;

import com.usmarinec.ledger.dto.CreateRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(
    name = "CreateJournalEntryLineRequest",
    description =
        "Request body for creating one journal entry line. A line should normally contain either a debit amount or a credit amount, but not both.")
public record CreateJournalEntryLineRequest(
    @Schema(
            description = "UUID of the account used by this line.",
            example = "2a2c0af1-28a1-4585-a6d2-92285f297c7c")
        @NotNull
        UUID accountId,
    @Schema(
            description = "Optional line-level description.",
            example = "Cash received",
            maxLength = 1000)
        @Size(max = 1000)
        String description,
    @Schema(description = "Debit amount for this line.", example = "100.00") @DecimalMin("0.00")
        BigDecimal debitAmount,
    @Schema(description = "Credit amount for this line.", example = "0.00") @DecimalMin("0.00")
        BigDecimal creditAmount)
    implements CreateRequest, JournalEntryLineRequest {}
