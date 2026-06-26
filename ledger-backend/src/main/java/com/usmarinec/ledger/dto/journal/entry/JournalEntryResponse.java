package com.usmarinec.ledger.dto.journal.entry;

import com.usmarinec.ledger.domain.journal.JournalEntryStatus;
import com.usmarinec.ledger.domain.journal.JournalEntryType;
import com.usmarinec.ledger.dto.DomainResponse;
import com.usmarinec.ledger.dto.Response;
import com.usmarinec.ledger.dto.journal.line.JournalEntryLineResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(name = "JournalEntryResponse", description = "Journal entry returned by the API.")
public record JournalEntryResponse(
    @Schema(
            description = "Unique journal entry identifier.",
            example = "e3f9f6c1-0f7d-4f1b-90be-263d2a6fa4fd")
        UUID id,
    @Schema(
            description = "Accounting entity identifier.",
            example = "550e8400-e29b-41d4-a716-446655440000")
        UUID accountingEntityId,
    @Schema(
            description = "Fiscal year identifier.",
            example = "9b680f75-7d7e-456b-9439-18fcdba3e6f1")
        UUID fiscalYearId,
    @Schema(description = "Journal entry date.", example = "2026-06-20") LocalDate entryDate,
    @Schema(description = "Journal entry type.", example = "STANDARD") JournalEntryType entryType,
    @Schema(description = "Journal entry status.", example = "DRAFT") JournalEntryStatus status,
    @Schema(description = "Journal entry memo.", example = "Record cash sale.") String memo,
    @Schema(
            description = "Date and time the journal entry was created.",
            example = "2026-06-20T14:30:00")
        LocalDateTime createdAt,
    @Schema(
            description = "Date and time the journal entry was posted, if posted.",
            example = "2026-06-20T15:00:00")
        LocalDateTime postedAt,
    @Schema(description = "Journal entry lines.") List<JournalEntryLineResponse> lines)
    implements Response, DomainResponse {}
