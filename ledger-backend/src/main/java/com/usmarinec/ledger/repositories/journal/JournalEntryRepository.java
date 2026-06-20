package com.usmarinec.ledger.repositories.journal;

import com.usmarinec.ledger.domain.journal.JournalEntry;
import com.usmarinec.ledger.repositories.LedgerRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;

public interface JournalEntryRepository extends LedgerRepository<JournalEntry> {
  List<JournalEntry> findByAccountingEntity_IdAndFiscalYear_IdOrderByEntryDateAsc(
      UUID accountingEntityId, UUID fiscalYearId);

  Optional<JournalEntry> findByIdAndAccountingEntity_Id(UUID id, UUID accountingEntityId);

  @EntityGraph(attributePaths = {"lines", "lines.account"})
  Optional<JournalEntry> findWithLinesById(UUID id);
}
