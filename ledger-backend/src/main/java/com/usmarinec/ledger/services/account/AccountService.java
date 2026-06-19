package com.usmarinec.ledger.services.account;

import com.usmarinec.ledger.domain.account.Account;
import com.usmarinec.ledger.domain.account.AccountType;
import com.usmarinec.ledger.domain.account.NormalBalance;
import com.usmarinec.ledger.domain.entities.AccountingEntity;
import com.usmarinec.ledger.dto.account.AccountResponse;
import com.usmarinec.ledger.dto.account.CreateAccountRequest;
import com.usmarinec.ledger.dto.account.UpdateAccountRequest;
import com.usmarinec.ledger.repositories.account.AccountRepository;
import com.usmarinec.ledger.repositories.entities.AccountingEntityRepository;
import com.usmarinec.ledger.services.LedgerService;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AccountService
    extends LedgerService<
        Account, AccountRepository, CreateAccountRequest, UpdateAccountRequest, AccountResponse> {
  @Autowired AccountingEntityRepository accountingEntityRepository;

  @Override
  protected Account createLedgerEntity(CreateAccountRequest request) {
    AccountingEntity accountingEntity = this.getAccountingEntity(request.accountingEntityId());
    this.validateNormalBalance(request.accountType(), request.normalBalance());
    if (this.repository.existsByAccountingEntity_IdAndCode(
        request.accountingEntityId(), request.code())) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Account already exists for this accounting entity");
    }

    Account account = new Account();
    account.setAccountingEntity(accountingEntity);
    account.setCode(request.code());
    account.setName(request.name());
    account.setAccountType(request.accountType());
    account.setNormalBalance(request.normalBalance());
    account.setClassification(request.classification());
    account.setActive(true);
    return account;
  }

  @Override
  protected void updateLedgerEntity(Account ledgerEntity, UpdateAccountRequest request) {
    UUID accountingEntityId = ledgerEntity.getAccountingEntity().getId();
    this.validateNormalBalance(request.accountType(), request.normalBalance());
    this.repository
        .findByAccountingEntity_IdAndCode(accountingEntityId, request.code())
        .filter(existing -> !existing.getId().equals(ledgerEntity.getId()))
        .ifPresent(
            existing -> {
              throw new ResponseStatusException(
                  HttpStatus.CONFLICT, "Account already exists for this accounting entity");
            });
    ledgerEntity.setCode(request.code());
    ledgerEntity.setName(request.name());
    ledgerEntity.setAccountType(request.accountType());
    ledgerEntity.setNormalBalance(request.normalBalance());
    ledgerEntity.setClassification(request.classification());
    ledgerEntity.setActive(request.active());
  }

  @Override
  protected AccountResponse toResponse(Account ledgerEntity) {
    return new AccountResponse(
        ledgerEntity.getId(),
        ledgerEntity.getAccountingEntity().getId(),
        ledgerEntity.getCode(),
        ledgerEntity.getName(),
        ledgerEntity.getAccountType(),
        ledgerEntity.getNormalBalance(),
        ledgerEntity.getClassification(),
        ledgerEntity.isActive());
  }

  /**
   * Returns a list of Accounts for a given AccountingEntity Id. @Param accountingEntityId UUID of
   * AccountingEntity @Return List<AccountResponse> response record
   */
  @Transactional(readOnly = true)
  public List<AccountResponse> findByAccountingEntity(UUID accountingEntityId) {
    return this.repository.findByAccountingEntity_IdOrderByCode(accountingEntityId).stream()
        .map(this::toResponse)
        .toList();
  }

  /**
   * Deactivates an account.
   *
   * @param id UUID of account to be deactivated
   */
  @Override
  @Transactional
  public void delete(UUID id) {
    Account account = this.getLedgerEntity(id);
    account.setActive(false);
    this.repository.save(account);
  }

  private AccountingEntity getAccountingEntity(UUID accountingEntityId) {
    return this.accountingEntityRepository
        .findById(accountingEntityId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Accounting entity not found"));
  }

  private void validateNormalBalance(AccountType accountType, NormalBalance normalBalance) {
    NormalBalance expectedNormalBalance =
        switch (accountType) {
          case ASSET, EXPENSE -> NormalBalance.DEBIT;
          case LIABILITY, EQUITY, REVENUE -> NormalBalance.CREDIT;
          default -> throw new IllegalArgumentException("Unexpected value: " + accountType);
        };

    if (normalBalance != expectedNormalBalance) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          accountType + " accounts must have a normal balance of " + expectedNormalBalance);
    }
  }
}
