package com.usmarinec.ledger.repositories.fiscal;

import com.usmarinec.ledger.domain.fiscal.FiscalYear;
import com.usmarinec.ledger.repositories.LedgerRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FiscalYearRepository extends LedgerRepository<FiscalYear> {
  List<FiscalYear> findByAccountingEntity_IdOrderByYearDesc(UUID accountingEntityId);

  Optional<FiscalYear> findByAccountingEntity_IdAndYear(UUID AccountingEntityId, Integer year);

  boolean existsByAccountingEntity_IdAndYear(UUID AccountingEntityId, Integer year);
}
