package com.usmarinec.ledger.services.entities;

import com.usmarinec.ledger.domain.entities.AccountingEntity;
import com.usmarinec.ledger.dto.entities.AccountingEntityResponse;
import com.usmarinec.ledger.dto.entities.CreateAccountingEntityRequest;
import com.usmarinec.ledger.dto.entities.UpdateAccountingEntityRequest;
import com.usmarinec.ledger.repositories.entities.AccountingEntityRepository;
import com.usmarinec.ledger.services.LedgerService;
import org.springframework.stereotype.Service;

@Service
public class AccountingEntityService
    extends LedgerService<
        AccountingEntity,
        AccountingEntityRepository,
        CreateAccountingEntityRequest,
        UpdateAccountingEntityRequest,
        AccountingEntityResponse> {
  @Override
  protected AccountingEntity createLedgerEntity(CreateAccountingEntityRequest request) {
    AccountingEntity accountingEntity = new AccountingEntity();
    accountingEntity.setName(request.name());
    return accountingEntity;
  }

  @Override
  protected void updateLedgerEntity(
      AccountingEntity ledgerEntity, UpdateAccountingEntityRequest request) {
    ledgerEntity.setName(request.name());
  }

  @Override
  protected AccountingEntityResponse toResponse(AccountingEntity ledgerEntity) {
    return new AccountingEntityResponse(
        ledgerEntity.getId(), ledgerEntity.getName(), ledgerEntity.getCreatedAt());
  }
}
