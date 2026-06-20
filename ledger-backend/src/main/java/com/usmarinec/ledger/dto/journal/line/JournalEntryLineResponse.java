package com.usmarinec.ledger.dto.journal.line;

import com.usmarinec.ledger.dto.Response;
import java.math.BigDecimal;
import java.util.UUID;

public record JournalEntryLineResponse(
    UUID id,
    Integer lineNumber,
    UUID accountId,
    String accountCode,
    String accountName,
    String description,
    BigDecimal debitAmount,
    BigDecimal creditAmount)
    implements Response {}
