package com.usmarinec.ledger.domain.journal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.usmarinec.ledger.domain.account.Account;
import com.usmarinec.ledger.domain.account.AccountClassification;
import com.usmarinec.ledger.domain.account.AccountType;
import com.usmarinec.ledger.domain.account.NormalBalance;
import com.usmarinec.ledger.domain.entities.AccountingEntity;
import com.usmarinec.ledger.domain.fiscal.FiscalYear;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JournalEntryLineTest {

  @Autowired private EntityManager entityManager;

  @Test
  void persistsJournalEntryLineWithGeneratedId() {
    AccountingEntity entity = createAccountingEntity("Test Entity");
    FiscalYear fiscalYear = createFiscalYear(entity, 2026);
    Account account = createAccount(entity, "1000", "Cash", AccountType.ASSET, NormalBalance.DEBIT);
    JournalEntry journalEntry = createJournalEntry(entity, fiscalYear);

    JournalEntryLine line = new JournalEntryLine();
    line.setJournalEntry(journalEntry);
    line.setAccount(account);
    line.setLineNumber(1);
    line.setDescription("Cash debit");
    line.setDebitAmount(new BigDecimal("100.00"));
    line.setCreditAmount(BigDecimal.ZERO);

    entityManager.persist(line);
    entityManager.flush();

    assertThat(line.getId()).isNotNull();
  }

  @Test
  void defaultsDebitAndCreditAmountsToZero() {
    JournalEntryLine line = new JournalEntryLine();

    assertThat(line.getDebitAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(line.getCreditAmount()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  void replacesNullDebitAmountWithZeroWhenCreditAmountIsPresent() {
    AccountingEntity entity = createAccountingEntity("Test Entity");
    FiscalYear fiscalYear = createFiscalYear(entity, 2026);
    Account account =
        createAccount(entity, "4000", "Revenue", AccountType.REVENUE, NormalBalance.CREDIT);
    JournalEntry journalEntry = createJournalEntry(entity, fiscalYear);

    JournalEntryLine line = new JournalEntryLine();
    line.setJournalEntry(journalEntry);
    line.setAccount(account);
    line.setLineNumber(1);
    line.setDebitAmount(null);
    line.setCreditAmount(new BigDecimal("100.00"));

    entityManager.persist(line);
    entityManager.flush();

    assertThat(line.getId()).isNotNull();
    assertThat(line.getDebitAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(line.getCreditAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
  }

  @Test
  void replacesNullCreditAmountWithZeroWhenDebitAmountIsPresent() {
    AccountingEntity entity = createAccountingEntity("Test Entity");
    FiscalYear fiscalYear = createFiscalYear(entity, 2026);
    Account account = createAccount(entity, "1000", "Cash", AccountType.ASSET, NormalBalance.DEBIT);
    JournalEntry journalEntry = createJournalEntry(entity, fiscalYear);

    JournalEntryLine line = new JournalEntryLine();
    line.setJournalEntry(journalEntry);
    line.setAccount(account);
    line.setLineNumber(1);
    line.setDebitAmount(new BigDecimal("100.00"));
    line.setCreditAmount(null);

    entityManager.persist(line);
    entityManager.flush();

    assertThat(line.getId()).isNotNull();
    assertThat(line.getDebitAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
    assertThat(line.getCreditAmount()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  void doesNotAllowBothDebitAndCreditAmountsToBeNull() {
    AccountingEntity entity = createAccountingEntity("Test Entity");
    FiscalYear fiscalYear = createFiscalYear(entity, 2026);
    Account account = createAccount(entity, "1000", "Cash", AccountType.ASSET, NormalBalance.DEBIT);
    JournalEntry journalEntry = createJournalEntry(entity, fiscalYear);

    JournalEntryLine line = new JournalEntryLine();
    line.setJournalEntry(journalEntry);
    line.setAccount(account);
    line.setLineNumber(1);
    line.setDebitAmount(null);
    line.setCreditAmount(null);

    entityManager.persist(line);

    assertThatThrownBy(() -> entityManager.flush()).isInstanceOf(RuntimeException.class);
  }

  @Test
  void doesNotAllowBothDebitAndCreditAmountsToBeZero() {
    AccountingEntity entity = createAccountingEntity("Test Entity");
    FiscalYear fiscalYear = createFiscalYear(entity, 2026);
    Account account = createAccount(entity, "1000", "Cash", AccountType.ASSET, NormalBalance.DEBIT);
    JournalEntry journalEntry = createJournalEntry(entity, fiscalYear);

    JournalEntryLine line = new JournalEntryLine();
    line.setJournalEntry(journalEntry);
    line.setAccount(account);
    line.setLineNumber(1);
    line.setDebitAmount(BigDecimal.ZERO);
    line.setCreditAmount(BigDecimal.ZERO);

    entityManager.persist(line);

    assertThatThrownBy(() -> entityManager.flush()).isInstanceOf(RuntimeException.class);
  }

  @Test
  void persistsJournalEntryLineAndCanReadItBack() {
    AccountingEntity entity = createAccountingEntity("Test Entity");
    FiscalYear fiscalYear = createFiscalYear(entity, 2026);
    Account account = createAccount(entity, "1000", "Cash", AccountType.ASSET, NormalBalance.DEBIT);
    JournalEntry journalEntry = createJournalEntry(entity, fiscalYear);

    JournalEntryLine line = new JournalEntryLine();
    line.setJournalEntry(journalEntry);
    line.setAccount(account);
    line.setLineNumber(1);
    line.setDescription("Cash debit");
    line.setDebitAmount(new BigDecimal("150.25"));
    line.setCreditAmount(BigDecimal.ZERO);

    entityManager.persist(line);
    entityManager.flush();

    UUID lineId = line.getId();

    entityManager.clear();

    JournalEntryLine found = entityManager.find(JournalEntryLine.class, lineId);

    assertThat(found).isNotNull();
    assertThat(found.getId()).isEqualTo(lineId);
    assertThat(found.getJournalEntry().getId()).isEqualTo(journalEntry.getId());
    assertThat(found.getAccount().getId()).isEqualTo(account.getId());
    assertThat(found.getLineNumber()).isEqualTo(1);
    assertThat(found.getDescription()).isEqualTo("Cash debit");
    assertThat(found.getDebitAmount()).isEqualByComparingTo(new BigDecimal("150.25"));
    assertThat(found.getCreditAmount()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  void persistsJournalEntryLineThroughJournalEntryCascade() {
    AccountingEntity entity = createAccountingEntity("Test Entity");
    FiscalYear fiscalYear = createFiscalYear(entity, 2026);
    final Account account =
        createAccount(entity, "1000", "Cash", AccountType.ASSET, NormalBalance.DEBIT);

    JournalEntry journalEntry = new JournalEntry();
    journalEntry.setEntity(entity);
    journalEntry.setFiscalYear(fiscalYear);
    journalEntry.setEntryDate(LocalDate.of(2026, 1, 15));

    JournalEntryLine line = new JournalEntryLine();
    line.setAccount(account);
    line.setLineNumber(1);
    line.setDescription("Cash debit");
    line.setDebitAmount(new BigDecimal("100.00"));
    line.setCreditAmount(BigDecimal.ZERO);

    journalEntry.addLine(line);

    entityManager.persist(journalEntry);
    entityManager.flush();

    assertThat(journalEntry.getId()).isNotNull();
    assertThat(line.getId()).isNotNull();
    assertThat(line.getJournalEntry()).isEqualTo(journalEntry);
    assertThat(journalEntry.getLines()).containsExactly(line);
  }

  @Test
  void doesNotAllowDuplicateLineNumberForSameJournalEntry() {
    AccountingEntity entity = createAccountingEntity("Test Entity");
    FiscalYear fiscalYear = createFiscalYear(entity, 2026);
    Account cashAccount =
        createAccount(entity, "1000", "Cash", AccountType.ASSET, NormalBalance.DEBIT);
    Account revenueAccount =
        createAccount(entity, "4000", "Revenue", AccountType.REVENUE, NormalBalance.CREDIT);
    JournalEntry journalEntry = createJournalEntry(entity, fiscalYear);

    createJournalEntryLine(
        journalEntry, cashAccount, 1, "Cash debit", new BigDecimal("100.00"), BigDecimal.ZERO);

    JournalEntryLine duplicate = new JournalEntryLine();
    duplicate.setJournalEntry(journalEntry);
    duplicate.setAccount(revenueAccount);
    duplicate.setLineNumber(1);
    duplicate.setDescription("Duplicate line number");
    duplicate.setDebitAmount(BigDecimal.ZERO);
    duplicate.setCreditAmount(new BigDecimal("100.00"));

    entityManager.persist(duplicate);

    assertThatThrownBy(() -> entityManager.flush()).isInstanceOf(RuntimeException.class);
  }

  @Test
  void allowsSameLineNumberForDifferentJournalEntries() {
    AccountingEntity entity = createAccountingEntity("Test Entity");
    FiscalYear fiscalYear = createFiscalYear(entity, 2026);
    Account cashAccount =
        createAccount(entity, "1000", "Cash", AccountType.ASSET, NormalBalance.DEBIT);

    JournalEntry firstJournalEntry = createJournalEntry(entity, fiscalYear);
    JournalEntry secondJournalEntry = createJournalEntry(entity, fiscalYear);

    JournalEntryLine firstLine =
        createJournalEntryLine(
            firstJournalEntry,
            cashAccount,
            1,
            "First entry line",
            new BigDecimal("100.00"),
            BigDecimal.ZERO);

    JournalEntryLine secondLine =
        createJournalEntryLine(
            secondJournalEntry,
            cashAccount,
            1,
            "Second entry line",
            new BigDecimal("200.00"),
            BigDecimal.ZERO);

    assertThat(firstLine.getId()).isNotNull();
    assertThat(secondLine.getId()).isNotNull();
    assertThat(firstLine.getLineNumber()).isEqualTo(secondLine.getLineNumber());
    assertThat(firstLine.getJournalEntry().getId())
        .isNotEqualTo(secondLine.getJournalEntry().getId());
  }

  private AccountingEntity createAccountingEntity(String name) {
    AccountingEntity entity = new AccountingEntity();
    entity.setName(name);

    entityManager.persist(entity);
    entityManager.flush();

    return entity;
  }

  private FiscalYear createFiscalYear(AccountingEntity entity, Integer year) {
    FiscalYear fiscalYear = new FiscalYear();
    fiscalYear.setAccountingEntity(entity);
    fiscalYear.setYear(year);
    fiscalYear.setStartDate(LocalDate.of(year, 1, 1));
    fiscalYear.setEndDate(LocalDate.of(year, 12, 31));

    entityManager.persist(fiscalYear);
    entityManager.flush();

    return fiscalYear;
  }

  private Account createAccount(
      AccountingEntity entity,
      String code,
      String name,
      AccountType accountType,
      NormalBalance normalBalance) {
    Account account = new Account();
    account.setEntity(entity);
    account.setCode(code);
    account.setName(name);
    account.setAccountType(accountType);
    account.setNormalBalance(normalBalance);
    account.setClassification(AccountClassification.NONE);

    entityManager.persist(account);
    entityManager.flush();

    return account;
  }

  private JournalEntry createJournalEntry(AccountingEntity entity, FiscalYear fiscalYear) {
    JournalEntry journalEntry = new JournalEntry();
    journalEntry.setEntity(entity);
    journalEntry.setFiscalYear(fiscalYear);
    journalEntry.setEntryDate(LocalDate.of(fiscalYear.getYear(), 1, 15));

    entityManager.persist(journalEntry);
    entityManager.flush();

    return journalEntry;
  }

  private JournalEntryLine createJournalEntryLine(
      JournalEntry journalEntry,
      Account account,
      Integer lineNumber,
      String description,
      BigDecimal debitAmount,
      BigDecimal creditAmount) {
    JournalEntryLine line = new JournalEntryLine();
    line.setJournalEntry(journalEntry);
    line.setAccount(account);
    line.setLineNumber(lineNumber);
    line.setDescription(description);
    line.setDebitAmount(debitAmount);
    line.setCreditAmount(creditAmount);

    entityManager.persist(line);
    entityManager.flush();

    return line;
  }
}
