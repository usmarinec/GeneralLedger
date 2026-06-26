package com.usmarinec.ledger.dto.journal.line;

import java.math.BigDecimal;
import java.util.UUID;

public interface JournalEntryLineRequest {
  UUID accountId();

  String description();

  BigDecimal debitAmount();

  BigDecimal creditAmount();
}
