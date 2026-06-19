package com.usmarinec.ledger.dto.journal.entry;

import com.usmarinec.ledger.domain.journal.JournalEntryType;
import com.usmarinec.ledger.dto.UpdateRequest;
import com.usmarinec.ledger.dto.journal.line.JournalEntryLineResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UpdateJournalEntryRequest(
    @NotNull UUID fiscalYearId,
    @NotNull LocalDate entryDate,
    @NotNull JournalEntryType entryType,
    @Size(max = 5000) String memo,
    @NotEmpty List<@Valid JournalEntryLineResponse> lines)
    implements UpdateRequest {}
