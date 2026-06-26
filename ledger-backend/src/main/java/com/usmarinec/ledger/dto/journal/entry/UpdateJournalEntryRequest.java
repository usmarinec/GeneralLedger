package com.usmarinec.ledger.dto.journal.entry;

import com.usmarinec.ledger.domain.journal.JournalEntryType;
import com.usmarinec.ledger.dto.UpdateRequest;
import com.usmarinec.ledger.dto.journal.line.UpdateJournalEntryLineRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(
    name = "UpdateJournalEntryRequest",
    description = "Request body for updating a draft journal entry and replacing its lines.")
public record UpdateJournalEntryRequest(
    @Schema(
            description = "UUID of the fiscal year.",
            example = "9b680f75-7d7e-456b-9439-18fcdba3e6f1")
        @NotNull
        UUID fiscalYearId,
    @Schema(description = "Updated journal entry date.", example = "2026-06-20") @NotNull
        LocalDate entryDate,
    @Schema(
            description = "Updated journal entry type.",
            example = "STANDARD",
            allowableValues = {"STANDARD", "ADJUSTING", "CLOSING"})
        @NotNull
        JournalEntryType entryType,
    @Schema(description = "Updated memo.", example = "Record cash sale.", maxLength = 5000)
        @Size(max = 5000)
        String memo,
    @Schema(description = "Replacement debit and credit lines for the journal entry.") @NotEmpty
        List<@Valid UpdateJournalEntryLineRequest> lines)
    implements UpdateRequest {}
