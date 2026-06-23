package com.usmarinec.ledger.dto.journal.line;

import com.usmarinec.ledger.dto.DomainResponse;
import com.usmarinec.ledger.dto.Response;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(
    name = "JournalEntryLineResponse",
    description =
        "Response body for  one journal entry line. A line should normally contain either a debit amount or a credit amount, but not both.")
public record JournalEntryLineResponse(
    @Schema(description = "UUID of this line.", example = "2a2c0af1-28a1-4585-a6d2-92285f297c7c")
        UUID id,
    @Schema(
            description =
                "Line number used to identify this line within the containing JournalEntry.",
            example = "1")
        Integer lineNumber,
    @Schema(
            description = "UUID of the account used by this line.",
            example = "2a2c0af1-28a1-4585-a6d2-92285f297c7c")
        UUID accountId,
    @Schema(description = "Code used to represent this account in the COA.", example = "100")
        String accountCode,
    @Schema(description = "Name of the account used by this line.", example = "cash")
        String accountName,
    @Schema(
            description = "Optional line-level description.",
            example = "Cash received",
            maxLength = 1000)
        String description,
    @Schema(description = "Debit amount for this line.", example = "100.00") BigDecimal debitAmount,
    @Schema(description = "Credit amount for this line.", example = "0.00") BigDecimal creditAmount)
    implements Response, DomainResponse {}
