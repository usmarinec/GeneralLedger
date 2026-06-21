package com.usmarinec.ledger.dto.journal.entry;

import com.usmarinec.ledger.domain.journal.JournalEntryType;
import com.usmarinec.ledger.dto.CreateRequest;
import com.usmarinec.ledger.dto.journal.line.CreateJournalEntryLineRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(
    name = "CreateJournalEntryRequest",
    description = "Request body for creating a draft journal entry with debit and credit lines.")
public record CreateJournalEntryRequest(
    @Schema(
            description = "UUID of the accounting entity.",
            example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull
        UUID accountingEntityId,
    @Schema(
            description = "UUID of the fiscal year.",
            example = "9b680f75-7d7e-456b-9439-18fcdba3e6f1")
        @NotNull
        UUID fiscalYearId,
    @Schema(description = "Date of the journal entry.", example = "2026-06-20") @NotNull
        LocalDate entryDate,
    @Schema(
            description = "Type of journal entry.",
            example = "STANDARD",
            allowableValues = {"STANDARD", "ADJUSTING", "CLOSING"})
        @NotNull
        JournalEntryType entryType,
    @Schema(
            description = "Optional memo describing the business purpose of the journal entry.",
            example = "Record cash sale.",
            maxLength = 5000)
        @Size(max = 5000)
        String memo,
    @Schema(
            description =
                "Debit and credit lines for the journal entry. Total debits must equal total credits.")
        @NotEmpty
        List<@Valid CreateJournalEntryLineRequest> lines)
    implements CreateRequest {}
