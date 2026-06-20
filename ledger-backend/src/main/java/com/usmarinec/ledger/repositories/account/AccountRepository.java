package com.usmarinec.ledger.repositories.account;

import com.usmarinec.ledger.domain.account.Account;
import com.usmarinec.ledger.repositories.LedgerRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends LedgerRepository<Account> {
  List<Account> findByAccountingEntity_IdOrderByCode(UUID accountingEntityId);

  Optional<Account> findByAccountingEntity_IdAndCode(UUID accountingEntityId, String code);

  boolean existsByAccountingEntity_IdAndCode(UUID accountingEntityId, String code);
}
