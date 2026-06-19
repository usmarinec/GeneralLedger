package com.usmarinec.ledger.dto.journal.entry;

import com.usmarinec.ledger.domain.journal.JournalEntryStatus;
import com.usmarinec.ledger.domain.journal.JournalEntryType;
import com.usmarinec.ledger.dto.Response;
import com.usmarinec.ledger.dto.journal.line.JournalEntryLineResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record JournalEntryResponse(
    UUID id,
    UUID accountingEntityId,
    UUID fiscalYearId,
    LocalDate entryDate,
    JournalEntryType entryType,
    JournalEntryStatus status,
    String memo,
    LocalDateTime createdAt,
    LocalDateTime postedAt,
    List<JournalEntryLineResponse> lines)
    implements Response {}
