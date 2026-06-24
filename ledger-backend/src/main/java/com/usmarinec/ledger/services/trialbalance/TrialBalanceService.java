package com.usmarinec.ledger.services.trialbalance;

import com.usmarinec.ledger.domain.entities.AccountingEntity;
import com.usmarinec.ledger.domain.fiscal.FiscalYear;
import com.usmarinec.ledger.domain.journal.JournalEntryStatus;
import com.usmarinec.ledger.domain.journal.JournalEntryType;
import com.usmarinec.ledger.dto.trialbalance.TrialBalanceLineResponse;
import com.usmarinec.ledger.dto.trialbalance.TrialBalanceResponse;
import com.usmarinec.ledger.exception.exceptions.BadRequestException;
import com.usmarinec.ledger.exception.exceptions.NotFoundException;
import com.usmarinec.ledger.repositories.entities.AccountingEntityRepository;
import com.usmarinec.ledger.repositories.fiscal.FiscalYearRepository;
import com.usmarinec.ledger.repositories.trialbalance.TrialBalanceLineProjection;
import com.usmarinec.ledger.repositories.trialbalance.TrialBalanceRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrialBalanceService {
  private static final String UNADJUSTED_TRIAL_BALANCE = "UNADJUSTED";

  @Autowired private TrialBalanceRepository trialBalanceRepository;
  @Autowired private AccountingEntityRepository accountingEntityRepository;
  @Autowired private FiscalYearRepository fiscalYearRepository;

  /**
   * Generates an unadjusted trial balance for the given accounting entity and fiscal year.
   *
   * <p>The unadjusted trial balance is calculated from posted {@code STANDARD} journal entries
   * only. Draft, voided, adjusting, and closing entries are excluded from this report.
   *
   * <p>Each returned line represents the summarized debit and credit activity for one account. The
   * service also calculates the total debit balance, total credit balance, and whether the trial
   * balance is balanced.
   *
   * @param accountingEntityId UUID of the accounting entity for which to generate the trial balance
   * @param fiscalYearId UUID of the fiscal year for which to generate the trial balance
   * @return trial balance response containing summarized account balances and report totals
   * @throws NotFoundException if the accounting entity or fiscal year cannot be found
   * @throws BadRequestException if the fiscal year does not belong to the accounting entity
   */
  @Transactional(readOnly = true)
  public TrialBalanceResponse generateUnadjustedTrialBalance(
      UUID accountingEntityId, UUID fiscalYearId) {
    AccountingEntity accountingEntity = getAccountingEntity(accountingEntityId);
    FiscalYear fiscalYear = getFiscalYear(fiscalYearId);

    validateFiscalYearBelongsToAccountingEntity(fiscalYear, accountingEntity);

    List<TrialBalanceLineResponse> lines =
        trialBalanceRepository
            .findTrialBalanceLines(
                accountingEntityId,
                fiscalYearId,
                JournalEntryStatus.POSTED,
                JournalEntryType.STANDARD)
            .stream()
            .map(this::toLineResponse)
            .toList();

    BigDecimal totalDebitBalance =
        lines.stream()
            .map(TrialBalanceLineResponse::debitBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalCreditBalance =
        lines.stream()
            .map(TrialBalanceLineResponse::creditBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    boolean balanced = totalDebitBalance.compareTo(totalCreditBalance) == 0;

    return new TrialBalanceResponse(
        accountingEntityId,
        fiscalYearId,
        UNADJUSTED_TRIAL_BALANCE,
        lines,
        totalDebitBalance,
        totalCreditBalance,
        balanced);
  }

  private TrialBalanceLineResponse toLineResponse(TrialBalanceLineProjection projection) {
    BigDecimal totalDebits = zeroIfNull(projection.getTotalDebits());
    BigDecimal totalCredits = zeroIfNull(projection.getTotalCredits());

    BigDecimal netDebitAmount = totalDebits.subtract(totalCredits);

    BigDecimal debitBalance =
        netDebitAmount.compareTo(BigDecimal.ZERO) > 0 ? netDebitAmount : BigDecimal.ZERO;

    BigDecimal creditBalance =
        netDebitAmount.compareTo(BigDecimal.ZERO) < 0 ? netDebitAmount.abs() : BigDecimal.ZERO;

    return new TrialBalanceLineResponse(
        projection.getAccountId(),
        projection.getAccountCode(),
        projection.getAccountName(),
        projection.getAccountType(),
        projection.getNormalBalance(),
        totalDebits,
        totalCredits,
        debitBalance,
        creditBalance);
  }

  private AccountingEntity getAccountingEntity(UUID accountingEntityId) {
    return accountingEntityRepository
        .findById(accountingEntityId)
        .orElseThrow(() -> new NotFoundException("Accounting entity not found"));
  }

  private FiscalYear getFiscalYear(UUID fiscalYearId) {
    return fiscalYearRepository
        .findById(fiscalYearId)
        .orElseThrow(() -> new NotFoundException("Fiscal year not found"));
  }

  private void validateFiscalYearBelongsToAccountingEntity(
      FiscalYear fiscalYear, AccountingEntity accountingEntity) {
    if (!fiscalYear.getAccountingEntity().getId().equals(accountingEntity.getId())) {
      throw new BadRequestException("Fiscal year does not belong to accounting entity");
    }
  }

  private BigDecimal zeroIfNull(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }
}
