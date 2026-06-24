package com.usmarinec.ledger.services.trialbalance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.usmarinec.ledger.domain.account.AccountType;
import com.usmarinec.ledger.domain.account.NormalBalance;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class TrialBalanceServiceTest {

  private TrialBalanceRepository trialBalanceRepository;
  private AccountingEntityRepository accountingEntityRepository;
  private FiscalYearRepository fiscalYearRepository;
  private TrialBalanceService service;

  @BeforeEach
  void setUp() {
    trialBalanceRepository = mock(TrialBalanceRepository.class);
    accountingEntityRepository = mock(AccountingEntityRepository.class);
    fiscalYearRepository = mock(FiscalYearRepository.class);

    service = new TrialBalanceService();

    /*
     * Spring is not running in this unit test, so @Autowired fields
     * are not populated automatically.
     */
    ReflectionTestUtils.setField(service, "trialBalanceRepository", trialBalanceRepository);
    ReflectionTestUtils.setField(service, "accountingEntityRepository", accountingEntityRepository);
    ReflectionTestUtils.setField(service, "fiscalYearRepository", fiscalYearRepository);
  }

  @Test
  void generateUnadjustedTrialBalance_whenPostedStandardEntriesExist_returnsBalancedTrialBalance() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID fiscalYearId = UUID.randomUUID();

    AccountingEntity accountingEntity = accountingEntity(accountingEntityId);
    FiscalYear fiscalYear = fiscalYear(fiscalYearId, accountingEntity);

    UUID cashAccountId = UUID.randomUUID();
    UUID revenueAccountId = UUID.randomUUID();

    TrialBalanceLineProjection cashProjection =
        projection(
            cashAccountId,
            "1000",
            "Cash",
            AccountType.ASSET,
            NormalBalance.DEBIT,
            new BigDecimal("100.00"),
            BigDecimal.ZERO);

    TrialBalanceLineProjection revenueProjection =
        projection(
            revenueAccountId,
            "4000",
            "Service Revenue",
            AccountType.REVENUE,
            NormalBalance.CREDIT,
            BigDecimal.ZERO,
            new BigDecimal("100.00"));

    when(accountingEntityRepository.findById(accountingEntityId))
        .thenReturn(Optional.of(accountingEntity));
    when(fiscalYearRepository.findById(fiscalYearId)).thenReturn(Optional.of(fiscalYear));
    when(trialBalanceRepository.findTrialBalanceLines(
            accountingEntityId, fiscalYearId, JournalEntryStatus.POSTED, JournalEntryType.STANDARD))
        .thenReturn(List.of(cashProjection, revenueProjection));

    TrialBalanceResponse response =
        service.generateUnadjustedTrialBalance(accountingEntityId, fiscalYearId);

    assertEquals(accountingEntityId, response.accountingEntityId());
    assertEquals(fiscalYearId, response.fiscalYearId());
    assertEquals("UNADJUSTED", response.trialBalanceType());
    assertTrue(response.balanced());
    assertEquals(new BigDecimal("100.00"), response.totalDebitBalance());
    assertEquals(new BigDecimal("100.00"), response.totalCreditBalance());
    assertEquals(2, response.lines().size());

    TrialBalanceLineResponse cashLine = response.lines().get(0);

    assertEquals(cashAccountId, cashLine.accountId());
    assertEquals("1000", cashLine.accountCode());
    assertEquals("Cash", cashLine.accountName());
    assertEquals(AccountType.ASSET, cashLine.accountType());
    assertEquals(NormalBalance.DEBIT, cashLine.normalBalance());
    assertEquals(new BigDecimal("100.00"), cashLine.totalDebits());
    assertEquals(BigDecimal.ZERO, cashLine.totalCredits());
    assertEquals(new BigDecimal("100.00"), cashLine.debitBalance());
    assertEquals(BigDecimal.ZERO, cashLine.creditBalance());

    TrialBalanceLineResponse revenueLine = response.lines().get(1);

    assertEquals(revenueAccountId, revenueLine.accountId());
    assertEquals("4000", revenueLine.accountCode());
    assertEquals("Service Revenue", revenueLine.accountName());
    assertEquals(AccountType.REVENUE, revenueLine.accountType());
    assertEquals(NormalBalance.CREDIT, revenueLine.normalBalance());
    assertEquals(BigDecimal.ZERO, revenueLine.totalDebits());
    assertEquals(new BigDecimal("100.00"), revenueLine.totalCredits());
    assertEquals(BigDecimal.ZERO, revenueLine.debitBalance());
    assertEquals(new BigDecimal("100.00"), revenueLine.creditBalance());

    verify(trialBalanceRepository)
        .findTrialBalanceLines(
            accountingEntityId, fiscalYearId, JournalEntryStatus.POSTED, JournalEntryType.STANDARD);
  }

  @Test
  void generateUnadjustedTrialBalance_whenDebitsDoNotEqualCredits_returnsUnbalancedResponse() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID fiscalYearId = UUID.randomUUID();

    AccountingEntity accountingEntity = accountingEntity(accountingEntityId);
    FiscalYear fiscalYear = fiscalYear(fiscalYearId, accountingEntity);

    TrialBalanceLineProjection cashProjection =
        projection(
            UUID.randomUUID(),
            "1000",
            "Cash",
            AccountType.ASSET,
            NormalBalance.DEBIT,
            new BigDecimal("150.00"),
            BigDecimal.ZERO);

    TrialBalanceLineProjection revenueProjection =
        projection(
            UUID.randomUUID(),
            "4000",
            "Service Revenue",
            AccountType.REVENUE,
            NormalBalance.CREDIT,
            BigDecimal.ZERO,
            new BigDecimal("100.00"));

    when(accountingEntityRepository.findById(accountingEntityId))
        .thenReturn(Optional.of(accountingEntity));
    when(fiscalYearRepository.findById(fiscalYearId)).thenReturn(Optional.of(fiscalYear));
    when(trialBalanceRepository.findTrialBalanceLines(
            accountingEntityId, fiscalYearId, JournalEntryStatus.POSTED, JournalEntryType.STANDARD))
        .thenReturn(List.of(cashProjection, revenueProjection));

    TrialBalanceResponse response =
        service.generateUnadjustedTrialBalance(accountingEntityId, fiscalYearId);

    assertFalse(response.balanced());
    assertEquals(new BigDecimal("150.00"), response.totalDebitBalance());
    assertEquals(new BigDecimal("100.00"), response.totalCreditBalance());
  }

  @Test
  void
      generateUnadjustedTrialBalance_whenNoPostedStandardEntriesExist_returnsEmptyBalancedResponse() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID fiscalYearId = UUID.randomUUID();

    AccountingEntity accountingEntity = accountingEntity(accountingEntityId);
    FiscalYear fiscalYear = fiscalYear(fiscalYearId, accountingEntity);

    when(accountingEntityRepository.findById(accountingEntityId))
        .thenReturn(Optional.of(accountingEntity));
    when(fiscalYearRepository.findById(fiscalYearId)).thenReturn(Optional.of(fiscalYear));
    when(trialBalanceRepository.findTrialBalanceLines(
            accountingEntityId, fiscalYearId, JournalEntryStatus.POSTED, JournalEntryType.STANDARD))
        .thenReturn(List.of());

    TrialBalanceResponse response =
        service.generateUnadjustedTrialBalance(accountingEntityId, fiscalYearId);

    assertEquals(accountingEntityId, response.accountingEntityId());
    assertEquals(fiscalYearId, response.fiscalYearId());
    assertEquals("UNADJUSTED", response.trialBalanceType());
    assertTrue(response.lines().isEmpty());
    assertEquals(BigDecimal.ZERO, response.totalDebitBalance());
    assertEquals(BigDecimal.ZERO, response.totalCreditBalance());
    assertTrue(response.balanced());
  }

  @Test
  void generateUnadjustedTrialBalance_whenProjectionTotalsAreNull_treatsNullsAsZero() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID fiscalYearId = UUID.randomUUID();

    AccountingEntity accountingEntity = accountingEntity(accountingEntityId);
    FiscalYear fiscalYear = fiscalYear(fiscalYearId, accountingEntity);

    TrialBalanceLineProjection projectionWithNullTotals =
        projection(
            UUID.randomUUID(), "1000", "Cash", AccountType.ASSET, NormalBalance.DEBIT, null, null);

    when(accountingEntityRepository.findById(accountingEntityId))
        .thenReturn(Optional.of(accountingEntity));
    when(fiscalYearRepository.findById(fiscalYearId)).thenReturn(Optional.of(fiscalYear));
    when(trialBalanceRepository.findTrialBalanceLines(
            accountingEntityId, fiscalYearId, JournalEntryStatus.POSTED, JournalEntryType.STANDARD))
        .thenReturn(List.of(projectionWithNullTotals));

    TrialBalanceResponse response =
        service.generateUnadjustedTrialBalance(accountingEntityId, fiscalYearId);

    assertEquals(1, response.lines().size());

    TrialBalanceLineResponse line = response.lines().get(0);

    assertEquals(BigDecimal.ZERO, line.totalDebits());
    assertEquals(BigDecimal.ZERO, line.totalCredits());
    assertEquals(BigDecimal.ZERO, line.debitBalance());
    assertEquals(BigDecimal.ZERO, line.creditBalance());
    assertTrue(response.balanced());
  }

  @Test
  void generateUnadjustedTrialBalance_whenAccountingEntityDoesNotExist_throwsNotFoundException() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID fiscalYearId = UUID.randomUUID();

    when(accountingEntityRepository.findById(accountingEntityId)).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () -> service.generateUnadjustedTrialBalance(accountingEntityId, fiscalYearId));

    verify(fiscalYearRepository, never()).findById(fiscalYearId);
    verify(trialBalanceRepository, never())
        .findTrialBalanceLines(
            accountingEntityId, fiscalYearId, JournalEntryStatus.POSTED, JournalEntryType.STANDARD);
  }

  @Test
  void generateUnadjustedTrialBalance_whenFiscalYearDoesNotExist_throwsNotFoundException() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID fiscalYearId = UUID.randomUUID();

    AccountingEntity accountingEntity = accountingEntity(accountingEntityId);

    when(accountingEntityRepository.findById(accountingEntityId))
        .thenReturn(Optional.of(accountingEntity));
    when(fiscalYearRepository.findById(fiscalYearId)).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () -> service.generateUnadjustedTrialBalance(accountingEntityId, fiscalYearId));

    verify(trialBalanceRepository, never())
        .findTrialBalanceLines(
            accountingEntityId, fiscalYearId, JournalEntryStatus.POSTED, JournalEntryType.STANDARD);
  }

  @Test
  void
      generateUnadjustedTrialBalance_whenFiscalYearBelongsToDifferentAccountingEntity_throwsBadRequestException() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID otherAccountingEntityId = UUID.randomUUID();
    UUID fiscalYearId = UUID.randomUUID();

    AccountingEntity accountingEntity = accountingEntity(accountingEntityId);
    AccountingEntity otherAccountingEntity = accountingEntity(otherAccountingEntityId);
    FiscalYear fiscalYear = fiscalYear(fiscalYearId, otherAccountingEntity);

    when(accountingEntityRepository.findById(accountingEntityId))
        .thenReturn(Optional.of(accountingEntity));
    when(fiscalYearRepository.findById(fiscalYearId)).thenReturn(Optional.of(fiscalYear));

    assertThrows(
        BadRequestException.class,
        () -> service.generateUnadjustedTrialBalance(accountingEntityId, fiscalYearId));

    verify(trialBalanceRepository, never())
        .findTrialBalanceLines(
            accountingEntityId, fiscalYearId, JournalEntryStatus.POSTED, JournalEntryType.STANDARD);
  }

  private AccountingEntity accountingEntity(UUID id) {
    AccountingEntity accountingEntity = new AccountingEntity();
    accountingEntity.setId(id);
    accountingEntity.setName("Test Accounting Entity");
    return accountingEntity;
  }

  private FiscalYear fiscalYear(UUID id, AccountingEntity accountingEntity) {
    FiscalYear fiscalYear = new FiscalYear();
    fiscalYear.setId(id);
    fiscalYear.setAccountingEntity(accountingEntity);
    fiscalYear.setYear(2026);
    fiscalYear.setStartDate(LocalDate.of(2026, 1, 1));
    fiscalYear.setEndDate(LocalDate.of(2026, 12, 31));
    fiscalYear.setClosed(false);
    return fiscalYear;
  }

  private TrialBalanceLineProjection projection(
      UUID accountId,
      String accountCode,
      String accountName,
      AccountType accountType,
      NormalBalance normalBalance,
      BigDecimal totalDebits,
      BigDecimal totalCredits) {
    return new TestTrialBalanceLineProjection(
        accountId, accountCode, accountName, accountType, normalBalance, totalDebits, totalCredits);
  }

  private record TestTrialBalanceLineProjection(
      UUID accountId,
      String accountCode,
      String accountName,
      AccountType accountType,
      NormalBalance normalBalance,
      BigDecimal totalDebits,
      BigDecimal totalCredits)
      implements TrialBalanceLineProjection {

    @Override
    public UUID getAccountId() {
      return accountId;
    }

    @Override
    public String getAccountCode() {
      return accountCode;
    }

    @Override
    public String getAccountName() {
      return accountName;
    }

    @Override
    public AccountType getAccountType() {
      return accountType;
    }

    @Override
    public NormalBalance getNormalBalance() {
      return normalBalance;
    }

    @Override
    public BigDecimal getTotalDebits() {
      return totalDebits;
    }

    @Override
    public BigDecimal getTotalCredits() {
      return totalCredits;
    }
  }
}
