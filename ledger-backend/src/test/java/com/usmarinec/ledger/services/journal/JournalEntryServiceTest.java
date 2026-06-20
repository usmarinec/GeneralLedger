package com.usmarinec.ledger.services.journal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.usmarinec.ledger.domain.account.Account;
import com.usmarinec.ledger.domain.account.AccountClassification;
import com.usmarinec.ledger.domain.account.AccountType;
import com.usmarinec.ledger.domain.account.NormalBalance;
import com.usmarinec.ledger.domain.entities.AccountingEntity;
import com.usmarinec.ledger.domain.fiscal.FiscalYear;
import com.usmarinec.ledger.domain.journal.JournalEntry;
import com.usmarinec.ledger.domain.journal.JournalEntryLine;
import com.usmarinec.ledger.domain.journal.JournalEntryStatus;
import com.usmarinec.ledger.domain.journal.JournalEntryType;
import com.usmarinec.ledger.dto.journal.entry.CreateJournalEntryRequest;
import com.usmarinec.ledger.dto.journal.entry.JournalEntryResponse;
import com.usmarinec.ledger.dto.journal.entry.UpdateJournalEntryRequest;
import com.usmarinec.ledger.dto.journal.line.CreateJournalEntryLineRequest;
import com.usmarinec.ledger.dto.journal.line.UpdateJournalEntryLineRequest;
import com.usmarinec.ledger.repositories.account.AccountRepository;
import com.usmarinec.ledger.repositories.entities.AccountingEntityRepository;
import com.usmarinec.ledger.repositories.fiscal.FiscalYearRepository;
import com.usmarinec.ledger.repositories.journal.JournalEntryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

class JournalEntryServiceTest {

  private JournalEntryRepository journalEntryRepository;
  private AccountingEntityRepository accountingEntityRepository;
  private FiscalYearRepository fiscalYearRepository;
  private AccountRepository accountRepository;
  private JournalEntryService service;

  @BeforeEach
  void setUp() {
    journalEntryRepository = mock(JournalEntryRepository.class);
    accountingEntityRepository = mock(AccountingEntityRepository.class);
    fiscalYearRepository = mock(FiscalYearRepository.class);
    accountRepository = mock(AccountRepository.class);

    service = new JournalEntryService();

    /*
     * Spring is not running in this unit test, so @Autowired fields
     * are not populated automatically.
     */
    ReflectionTestUtils.setField(service, "repository", journalEntryRepository);
    ReflectionTestUtils.setField(service, "accountingEntityRepository", accountingEntityRepository);
    ReflectionTestUtils.setField(service, "fiscalYearRepository", fiscalYearRepository);
    ReflectionTestUtils.setField(service, "accountRepository", accountRepository);
  }

  @Test
  void create_whenRequestIsValid_savesJournalEntryWithLinesAndReturnsResponse() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID fiscalYearId = UUID.randomUUID();
    UUID journalEntryId = UUID.randomUUID();
    UUID cashAccountId = UUID.randomUUID();
    UUID equityAccountId = UUID.randomUUID();

    AccountingEntity accountingEntity = accountingEntity(accountingEntityId);
    FiscalYear fiscalYear = fiscalYear(fiscalYearId, accountingEntity, false);
    Account cash = account(cashAccountId, accountingEntity, "1000", "Cash", true);
    Account equity = account(equityAccountId, accountingEntity, "3000", "Owner's Equity", true);

    CreateJournalEntryRequest request =
        new CreateJournalEntryRequest(
            accountingEntityId,
            fiscalYearId,
            LocalDate.of(2026, 1, 1),
            JournalEntryType.STANDARD,
            "Owner contribution",
            List.of(
                new CreateJournalEntryLineRequest(
                    cashAccountId, "Cash received", new BigDecimal("1000.00"), BigDecimal.ZERO),
                new CreateJournalEntryLineRequest(
                    equityAccountId,
                    "Owner contribution",
                    BigDecimal.ZERO,
                    new BigDecimal("1000.00"))));

    when(accountingEntityRepository.findById(accountingEntityId))
        .thenReturn(Optional.of(accountingEntity));
    when(fiscalYearRepository.findById(fiscalYearId)).thenReturn(Optional.of(fiscalYear));
    when(accountRepository.findById(cashAccountId)).thenReturn(Optional.of(cash));
    when(accountRepository.findById(equityAccountId)).thenReturn(Optional.of(equity));

    when(journalEntryRepository.save(any(JournalEntry.class)))
        .thenAnswer(
            invocation -> {
              JournalEntry journalEntry = invocation.getArgument(0);
              journalEntry.setId(journalEntryId);
              journalEntry.setCreatedAt(LocalDateTime.of(2026, 1, 1, 12, 0));
              return journalEntry;
            });

    JournalEntryResponse response = service.create(request);

    assertEquals(journalEntryId, response.id());
    assertEquals(accountingEntityId, response.accountingEntityId());
    assertEquals(fiscalYearId, response.fiscalYearId());
    assertEquals(LocalDate.of(2026, 1, 1), response.entryDate());
    assertEquals(JournalEntryType.STANDARD, response.entryType());
    assertEquals(JournalEntryStatus.DRAFT, response.status());
    assertEquals("Owner contribution", response.memo());
    assertEquals(2, response.lines().size());

    assertEquals(1, response.lines().get(0).lineNumber());
    assertEquals(cashAccountId, response.lines().get(0).accountId());
    assertEquals("1000", response.lines().get(0).accountCode());
    assertEquals(new BigDecimal("1000.00"), response.lines().get(0).debitAmount());
    assertEquals(BigDecimal.ZERO, response.lines().get(0).creditAmount());

    assertEquals(2, response.lines().get(1).lineNumber());
    assertEquals(equityAccountId, response.lines().get(1).accountId());
    assertEquals("3000", response.lines().get(1).accountCode());
    assertEquals(BigDecimal.ZERO, response.lines().get(1).debitAmount());
    assertEquals(new BigDecimal("1000.00"), response.lines().get(1).creditAmount());

    ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
    verify(journalEntryRepository).save(captor.capture());

    JournalEntry savedJournalEntry = captor.getValue();

    assertEquals(accountingEntity, savedJournalEntry.getAccountingEntity());
    assertEquals(fiscalYear, savedJournalEntry.getFiscalYear());
    assertEquals(JournalEntryStatus.DRAFT, savedJournalEntry.getStatus());
    assertEquals(2, savedJournalEntry.getLines().size());

    JournalEntryLine firstLine = savedJournalEntry.getLines().get(0);
    assertEquals(savedJournalEntry, firstLine.getJournalEntry());
    assertEquals(cash, firstLine.getAccount());
    assertEquals(1, firstLine.getLineNumber());

    JournalEntryLine secondLine = savedJournalEntry.getLines().get(1);
    assertEquals(savedJournalEntry, secondLine.getJournalEntry());
    assertEquals(equity, secondLine.getAccount());
    assertEquals(2, secondLine.getLineNumber());
  }

  @Test
  void create_whenJournalEntryIsUnbalanced_throwsBadRequest() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID fiscalYearId = UUID.randomUUID();
    UUID cashAccountId = UUID.randomUUID();
    UUID equityAccountId = UUID.randomUUID();

    AccountingEntity accountingEntity = accountingEntity(accountingEntityId);
    FiscalYear fiscalYear = fiscalYear(fiscalYearId, accountingEntity, false);

    CreateJournalEntryRequest request =
        new CreateJournalEntryRequest(
            accountingEntityId,
            fiscalYearId,
            LocalDate.of(2026, 1, 1),
            JournalEntryType.STANDARD,
            "Bad entry",
            List.of(
                new CreateJournalEntryLineRequest(
                    cashAccountId, "Cash received", new BigDecimal("1000.00"), BigDecimal.ZERO),
                new CreateJournalEntryLineRequest(
                    equityAccountId,
                    "Owner contribution",
                    BigDecimal.ZERO,
                    new BigDecimal("900.00"))));

    when(accountingEntityRepository.findById(accountingEntityId))
        .thenReturn(Optional.of(accountingEntity));
    when(fiscalYearRepository.findById(fiscalYearId)).thenReturn(Optional.of(fiscalYear));

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> service.create(request));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    verify(journalEntryRepository, never()).save(any(JournalEntry.class));
  }

  @Test
  void create_whenFiscalYearDoesNotBelongToAccountingEntity_throwsBadRequest() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID otherAccountingEntityId = UUID.randomUUID();
    UUID fiscalYearId = UUID.randomUUID();
    UUID cashAccountId = UUID.randomUUID();
    UUID equityAccountId = UUID.randomUUID();

    AccountingEntity accountingEntity = accountingEntity(accountingEntityId);
    AccountingEntity otherAccountingEntity = accountingEntity(otherAccountingEntityId);
    FiscalYear fiscalYear = fiscalYear(fiscalYearId, otherAccountingEntity, false);

    CreateJournalEntryRequest request =
        balancedCreateRequest(accountingEntityId, fiscalYearId, cashAccountId, equityAccountId);

    when(accountingEntityRepository.findById(accountingEntityId))
        .thenReturn(Optional.of(accountingEntity));
    when(fiscalYearRepository.findById(fiscalYearId)).thenReturn(Optional.of(fiscalYear));

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> service.create(request));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    verify(journalEntryRepository, never()).save(any(JournalEntry.class));
  }

  @Test
  void create_whenFiscalYearIsClosed_throwsBadRequest() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID fiscalYearId = UUID.randomUUID();
    UUID cashAccountId = UUID.randomUUID();
    UUID equityAccountId = UUID.randomUUID();

    AccountingEntity accountingEntity = accountingEntity(accountingEntityId);
    FiscalYear fiscalYear = fiscalYear(fiscalYearId, accountingEntity, true);

    CreateJournalEntryRequest request =
        balancedCreateRequest(accountingEntityId, fiscalYearId, cashAccountId, equityAccountId);

    when(accountingEntityRepository.findById(accountingEntityId))
        .thenReturn(Optional.of(accountingEntity));
    when(fiscalYearRepository.findById(fiscalYearId)).thenReturn(Optional.of(fiscalYear));

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> service.create(request));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    verify(journalEntryRepository, never()).save(any(JournalEntry.class));
  }

  @Test
  void create_whenAccountIsInactive_throwsBadRequest() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID fiscalYearId = UUID.randomUUID();
    UUID cashAccountId = UUID.randomUUID();
    UUID equityAccountId = UUID.randomUUID();

    AccountingEntity accountingEntity = accountingEntity(accountingEntityId);
    FiscalYear fiscalYear = fiscalYear(fiscalYearId, accountingEntity, false);
    Account inactiveCash = account(cashAccountId, accountingEntity, "1000", "Cash", false);

    CreateJournalEntryRequest request =
        balancedCreateRequest(accountingEntityId, fiscalYearId, cashAccountId, equityAccountId);

    when(accountingEntityRepository.findById(accountingEntityId))
        .thenReturn(Optional.of(accountingEntity));
    when(fiscalYearRepository.findById(fiscalYearId)).thenReturn(Optional.of(fiscalYear));
    when(accountRepository.findById(cashAccountId)).thenReturn(Optional.of(inactiveCash));

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> service.create(request));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    verify(journalEntryRepository, never()).save(any(JournalEntry.class));
  }

  @Test
  void update_whenDraftEntryIsValid_replacesHeaderAndLines() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID fiscalYearId = UUID.randomUUID();
    UUID journalEntryId = UUID.randomUUID();
    UUID oldAccountId = UUID.randomUUID();
    UUID cashAccountId = UUID.randomUUID();
    UUID revenueAccountId = UUID.randomUUID();

    AccountingEntity accountingEntity = accountingEntity(accountingEntityId);
    FiscalYear fiscalYear = fiscalYear(fiscalYearId, accountingEntity, false);

    Account oldAccount = account(oldAccountId, accountingEntity, "9999", "Old Account", true);
    Account cash = account(cashAccountId, accountingEntity, "1000", "Cash", true);
    Account revenue = account(revenueAccountId, accountingEntity, "4000", "Service Revenue", true);

    JournalEntry existingJournalEntry =
        journalEntry(
            journalEntryId,
            accountingEntity,
            fiscalYear,
            JournalEntryStatus.DRAFT,
            List.of(line(oldAccount, 1, new BigDecimal("50.00"), BigDecimal.ZERO)));

    UpdateJournalEntryRequest request =
        new UpdateJournalEntryRequest(
            fiscalYearId,
            LocalDate.of(2026, 2, 1),
            JournalEntryType.ADJUSTING,
            "Updated entry",
            List.of(
                new UpdateJournalEntryLineRequest(
                    cashAccountId, "Cash received", new BigDecimal("250.00"), BigDecimal.ZERO),
                new UpdateJournalEntryLineRequest(
                    revenueAccountId,
                    "Service revenue",
                    BigDecimal.ZERO,
                    new BigDecimal("250.00"))));

    when(journalEntryRepository.findWithLinesById(journalEntryId))
        .thenReturn(Optional.of(existingJournalEntry));
    when(fiscalYearRepository.findById(fiscalYearId)).thenReturn(Optional.of(fiscalYear));
    when(accountRepository.findById(cashAccountId)).thenReturn(Optional.of(cash));
    when(accountRepository.findById(revenueAccountId)).thenReturn(Optional.of(revenue));
    when(journalEntryRepository.save(existingJournalEntry)).thenReturn(existingJournalEntry);

    JournalEntryResponse response = service.update(journalEntryId, request);

    assertEquals(journalEntryId, response.id());
    assertEquals(fiscalYearId, response.fiscalYearId());
    assertEquals(LocalDate.of(2026, 2, 1), response.entryDate());
    assertEquals(JournalEntryType.ADJUSTING, response.entryType());
    assertEquals(JournalEntryStatus.DRAFT, response.status());
    assertEquals("Updated entry", response.memo());
    assertEquals(2, response.lines().size());

    assertEquals(1, response.lines().get(0).lineNumber());
    assertEquals(cashAccountId, response.lines().get(0).accountId());
    assertEquals(new BigDecimal("250.00"), response.lines().get(0).debitAmount());

    assertEquals(2, response.lines().get(1).lineNumber());
    assertEquals(revenueAccountId, response.lines().get(1).accountId());
    assertEquals(new BigDecimal("250.00"), response.lines().get(1).creditAmount());

    assertEquals(2, existingJournalEntry.getLines().size());
    assertEquals(cash, existingJournalEntry.getLines().get(0).getAccount());
    assertEquals(revenue, existingJournalEntry.getLines().get(1).getAccount());

    verify(journalEntryRepository).save(existingJournalEntry);
  }

  @Test
  void update_whenEntryIsPosted_throwsBadRequest() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID fiscalYearId = UUID.randomUUID();
    UUID journalEntryId = UUID.randomUUID();
    UUID cashAccountId = UUID.randomUUID();
    UUID equityAccountId = UUID.randomUUID();

    AccountingEntity accountingEntity = accountingEntity(accountingEntityId);
    FiscalYear fiscalYear = fiscalYear(fiscalYearId, accountingEntity, false);

    JournalEntry postedJournalEntry =
        journalEntry(
            journalEntryId, accountingEntity, fiscalYear, JournalEntryStatus.POSTED, List.of());

    UpdateJournalEntryRequest request =
        balancedUpdateRequest(fiscalYearId, cashAccountId, equityAccountId);

    when(journalEntryRepository.findWithLinesById(journalEntryId))
        .thenReturn(Optional.of(postedJournalEntry));

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> service.update(journalEntryId, request));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    verify(journalEntryRepository, never()).save(any(JournalEntry.class));
  }

  @Test
  void update_whenEntryIsVoided_throwsBadRequest() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID fiscalYearId = UUID.randomUUID();
    UUID journalEntryId = UUID.randomUUID();
    UUID cashAccountId = UUID.randomUUID();
    UUID equityAccountId = UUID.randomUUID();

    AccountingEntity accountingEntity = accountingEntity(accountingEntityId);
    FiscalYear fiscalYear = fiscalYear(fiscalYearId, accountingEntity, false);

    JournalEntry voidedJournalEntry =
        journalEntry(
            journalEntryId, accountingEntity, fiscalYear, JournalEntryStatus.VOIDED, List.of());

    UpdateJournalEntryRequest request =
        balancedUpdateRequest(fiscalYearId, cashAccountId, equityAccountId);

    when(journalEntryRepository.findWithLinesById(journalEntryId))
        .thenReturn(Optional.of(voidedJournalEntry));

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> service.update(journalEntryId, request));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    verify(journalEntryRepository, never()).save(any(JournalEntry.class));
  }

  @Test
  void post_whenDraftEntryIsBalanced_setsStatusPostedAndPostedAt() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID fiscalYearId = UUID.randomUUID();
    UUID journalEntryId = UUID.randomUUID();

    AccountingEntity accountingEntity = accountingEntity(accountingEntityId);
    FiscalYear fiscalYear = fiscalYear(fiscalYearId, accountingEntity, false);

    Account cash = account(UUID.randomUUID(), accountingEntity, "1000", "Cash", true);
    Account equity = account(UUID.randomUUID(), accountingEntity, "3000", "Owner's Equity", true);

    JournalEntry journalEntry =
        journalEntry(
            journalEntryId,
            accountingEntity,
            fiscalYear,
            JournalEntryStatus.DRAFT,
            List.of(
                line(cash, 1, new BigDecimal("1000.00"), BigDecimal.ZERO),
                line(equity, 2, BigDecimal.ZERO, new BigDecimal("1000.00"))));

    when(journalEntryRepository.findWithLinesById(journalEntryId))
        .thenReturn(Optional.of(journalEntry));
    when(journalEntryRepository.save(journalEntry)).thenReturn(journalEntry);

    JournalEntryResponse response = service.post(journalEntryId);

    assertEquals(JournalEntryStatus.POSTED, response.status());
    assertEquals(JournalEntryStatus.POSTED, journalEntry.getStatus());
    assertNotNull(journalEntry.getPostedAt());

    verify(journalEntryRepository).save(journalEntry);
  }

  @Test
  void post_whenEntryAlreadyPosted_throwsBadRequest() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID fiscalYearId = UUID.randomUUID();
    UUID journalEntryId = UUID.randomUUID();

    AccountingEntity accountingEntity = accountingEntity(accountingEntityId);
    FiscalYear fiscalYear = fiscalYear(fiscalYearId, accountingEntity, false);

    JournalEntry journalEntry =
        journalEntry(
            journalEntryId, accountingEntity, fiscalYear, JournalEntryStatus.POSTED, List.of());

    when(journalEntryRepository.findWithLinesById(journalEntryId))
        .thenReturn(Optional.of(journalEntry));

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> service.post(journalEntryId));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    verify(journalEntryRepository, never()).save(any(JournalEntry.class));
  }

  @Test
  void post_whenFiscalYearIsClosed_throwsBadRequest() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID fiscalYearId = UUID.randomUUID();
    UUID journalEntryId = UUID.randomUUID();

    AccountingEntity accountingEntity = accountingEntity(accountingEntityId);
    FiscalYear fiscalYear = fiscalYear(fiscalYearId, accountingEntity, true);

    JournalEntry journalEntry =
        journalEntry(
            journalEntryId, accountingEntity, fiscalYear, JournalEntryStatus.DRAFT, List.of());

    when(journalEntryRepository.findWithLinesById(journalEntryId))
        .thenReturn(Optional.of(journalEntry));

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> service.post(journalEntryId));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    verify(journalEntryRepository, never()).save(any(JournalEntry.class));
  }

  @Test
  void delete_whenEntryExists_setsStatusVoidedAndSaves() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID fiscalYearId = UUID.randomUUID();
    UUID journalEntryId = UUID.randomUUID();

    AccountingEntity accountingEntity = accountingEntity(accountingEntityId);
    FiscalYear fiscalYear = fiscalYear(fiscalYearId, accountingEntity, false);

    JournalEntry journalEntry =
        journalEntry(
            journalEntryId, accountingEntity, fiscalYear, JournalEntryStatus.DRAFT, List.of());

    when(journalEntryRepository.findWithLinesById(journalEntryId))
        .thenReturn(Optional.of(journalEntry));
    when(journalEntryRepository.save(journalEntry)).thenReturn(journalEntry);

    service.delete(journalEntryId);

    assertEquals(JournalEntryStatus.VOIDED, journalEntry.getStatus());

    verify(journalEntryRepository).save(journalEntry);
    verify(journalEntryRepository, never()).delete(any(JournalEntry.class));
  }

  @Test
  void findByAccountingEntityAndFiscalYear_returnsMatchingEntries() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID fiscalYearId = UUID.randomUUID();

    AccountingEntity accountingEntity = accountingEntity(accountingEntityId);
    FiscalYear fiscalYear = fiscalYear(fiscalYearId, accountingEntity, false);

    Account cash = account(UUID.randomUUID(), accountingEntity, "1000", "Cash", true);
    Account equity = account(UUID.randomUUID(), accountingEntity, "3000", "Owner's Equity", true);

    JournalEntry first =
        journalEntry(
            UUID.randomUUID(),
            accountingEntity,
            fiscalYear,
            JournalEntryStatus.DRAFT,
            List.of(
                line(cash, 1, new BigDecimal("100.00"), BigDecimal.ZERO),
                line(equity, 2, BigDecimal.ZERO, new BigDecimal("100.00"))));

    JournalEntry second =
        journalEntry(
            UUID.randomUUID(),
            accountingEntity,
            fiscalYear,
            JournalEntryStatus.POSTED,
            List.of(
                line(cash, 1, new BigDecimal("200.00"), BigDecimal.ZERO),
                line(equity, 2, BigDecimal.ZERO, new BigDecimal("200.00"))));

    when(journalEntryRepository.findByAccountingEntity_IdAndFiscalYear_IdOrderByEntryDateAsc(
            accountingEntityId, fiscalYearId))
        .thenReturn(List.of(first, second));

    List<JournalEntryResponse> responses =
        service.findByAccountingEntityAndFiscalYear(accountingEntityId, fiscalYearId);

    assertEquals(2, responses.size());

    assertEquals(first.getId(), responses.get(0).id());
    assertEquals(accountingEntityId, responses.get(0).accountingEntityId());
    assertEquals(fiscalYearId, responses.get(0).fiscalYearId());

    assertEquals(second.getId(), responses.get(1).id());
    assertEquals(accountingEntityId, responses.get(1).accountingEntityId());
    assertEquals(fiscalYearId, responses.get(1).fiscalYearId());

    verify(journalEntryRepository)
        .findByAccountingEntity_IdAndFiscalYear_IdOrderByEntryDateAsc(
            accountingEntityId, fiscalYearId);
  }

  private CreateJournalEntryRequest balancedCreateRequest(
      UUID accountingEntityId, UUID fiscalYearId, UUID debitAccountId, UUID creditAccountId) {
    return new CreateJournalEntryRequest(
        accountingEntityId,
        fiscalYearId,
        LocalDate.of(2026, 1, 1),
        JournalEntryType.STANDARD,
        "Balanced entry",
        List.of(
            new CreateJournalEntryLineRequest(
                debitAccountId, "Debit line", new BigDecimal("100.00"), BigDecimal.ZERO),
            new CreateJournalEntryLineRequest(
                creditAccountId, "Credit line", BigDecimal.ZERO, new BigDecimal("100.00"))));
  }

  private UpdateJournalEntryRequest balancedUpdateRequest(
      UUID fiscalYearId, UUID debitAccountId, UUID creditAccountId) {
    return new UpdateJournalEntryRequest(
        fiscalYearId,
        LocalDate.of(2026, 1, 1),
        JournalEntryType.STANDARD,
        "Balanced update",
        List.of(
            new UpdateJournalEntryLineRequest(
                debitAccountId, "Debit line", new BigDecimal("100.00"), BigDecimal.ZERO),
            new UpdateJournalEntryLineRequest(
                creditAccountId, "Credit line", BigDecimal.ZERO, new BigDecimal("100.00"))));
  }

  private AccountingEntity accountingEntity(UUID id) {
    AccountingEntity accountingEntity = new AccountingEntity();
    accountingEntity.setId(id);
    accountingEntity.setName("Test Accounting Entity");
    return accountingEntity;
  }

  private FiscalYear fiscalYear(UUID id, AccountingEntity accountingEntity, boolean closed) {
    FiscalYear fiscalYear = new FiscalYear();
    fiscalYear.setId(id);
    fiscalYear.setAccountingEntity(accountingEntity);
    fiscalYear.setYear(2026);
    fiscalYear.setStartDate(LocalDate.of(2026, 1, 1));
    fiscalYear.setEndDate(LocalDate.of(2026, 12, 31));
    fiscalYear.setClosed(closed);
    return fiscalYear;
  }

  private Account account(
      UUID id, AccountingEntity accountingEntity, String code, String name, boolean active) {
    Account account = new Account();
    account.setId(id);
    account.setAccountingEntity(accountingEntity);
    account.setCode(code);
    account.setName(name);
    account.setAccountType(AccountType.ASSET);
    account.setNormalBalance(NormalBalance.DEBIT);
    account.setClassification(AccountClassification.CURRENT);
    account.setActive(active);
    return account;
  }

  private JournalEntry journalEntry(
      UUID id,
      AccountingEntity accountingEntity,
      FiscalYear fiscalYear,
      JournalEntryStatus status,
      List<JournalEntryLine> lines) {
    JournalEntry journalEntry = new JournalEntry();
    journalEntry.setId(id);
    journalEntry.setAccountingEntity(accountingEntity);
    journalEntry.setFiscalYear(fiscalYear);
    journalEntry.setEntryDate(LocalDate.of(2026, 1, 1));
    journalEntry.setEntryType(JournalEntryType.STANDARD);
    journalEntry.setStatus(status);
    journalEntry.setMemo("Test journal entry");
    journalEntry.setCreatedAt(LocalDateTime.of(2026, 1, 1, 12, 0));
    journalEntry.setPostedAt(null);

    for (JournalEntryLine line : lines) {
      journalEntry.addLine(line);
    }

    return journalEntry;
  }

  private JournalEntryLine line(
      Account account, int lineNumber, BigDecimal debitAmount, BigDecimal creditAmount) {
    JournalEntryLine line = new JournalEntryLine();
    line.setId(UUID.randomUUID());
    line.setAccount(account);
    line.setLineNumber(lineNumber);
    line.setDescription("Line " + lineNumber);
    line.setDebitAmount(debitAmount);
    line.setCreditAmount(creditAmount);
    return line;
  }
}
