package com.usmarinec.ledger.services.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.usmarinec.ledger.domain.account.Account;
import com.usmarinec.ledger.domain.account.AccountClassification;
import com.usmarinec.ledger.domain.account.AccountType;
import com.usmarinec.ledger.domain.account.NormalBalance;
import com.usmarinec.ledger.domain.entities.AccountingEntity;
import com.usmarinec.ledger.dto.account.AccountResponse;
import com.usmarinec.ledger.dto.account.CreateAccountRequest;
import com.usmarinec.ledger.dto.account.UpdateAccountRequest;
import com.usmarinec.ledger.repositories.account.AccountRepository;
import com.usmarinec.ledger.repositories.entities.AccountingEntityRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

class AccountServiceTest {

  private AccountRepository accountRepository;
  private AccountingEntityRepository accountingEntityRepository;
  private AccountService service;

  @BeforeEach
  void setUp() {
    accountRepository = mock(AccountRepository.class);
    accountingEntityRepository = mock(AccountingEntityRepository.class);

    service = new AccountService();

    /*
     * Spring is not running in this unit test, so @Autowired fields
     * are not populated automatically.
     */
    ReflectionTestUtils.setField(service, "repository", accountRepository);
    ReflectionTestUtils.setField(service, "accountingEntityRepository", accountingEntityRepository);
  }

  @Test
  void create_whenRequestIsValid_savesAccountAndReturnsResponse() {
    UUID accountingEntityId = UUID.randomUUID();
    final UUID accountId = UUID.randomUUID();

    AccountingEntity accountingEntity = new AccountingEntity();
    accountingEntity.setId(accountingEntityId);

    CreateAccountRequest request =
        new CreateAccountRequest(
            accountingEntityId,
            "1000",
            "Cash",
            AccountType.ASSET,
            NormalBalance.DEBIT,
            AccountClassification.CURRENT);

    when(accountingEntityRepository.findById(accountingEntityId))
        .thenReturn(Optional.of(accountingEntity));

    when(accountRepository.existsByAccountingEntity_IdAndCode(accountingEntityId, "1000"))
        .thenReturn(false);

    when(accountRepository.save(any(Account.class)))
        .thenAnswer(
            invocation -> {
              Account account = invocation.getArgument(0);
              account.setId(accountId);
              return account;
            });

    AccountResponse response = service.create(request);

    assertEquals(accountId, response.id());
    assertEquals(accountingEntityId, response.accountingEntityId());
    assertEquals("1000", response.code());
    assertEquals("Cash", response.name());
    assertEquals(AccountType.ASSET, response.accountType());
    assertEquals(NormalBalance.DEBIT, response.normalBalance());
    assertEquals(AccountClassification.CURRENT, response.classification());
    assertTrue(response.active());

    ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
    verify(accountRepository).save(captor.capture());

    Account savedAccount = captor.getValue();

    assertEquals(accountingEntity, savedAccount.getAccountingEntity());
    assertEquals("1000", savedAccount.getCode());
    assertEquals("Cash", savedAccount.getName());
    assertEquals(AccountType.ASSET, savedAccount.getAccountType());
    assertEquals(NormalBalance.DEBIT, savedAccount.getNormalBalance());
    assertEquals(AccountClassification.CURRENT, savedAccount.getClassification());
    assertTrue(savedAccount.isActive());
  }

  @Test
  void create_whenAccountingEntityDoesNotExist_throwsNotFound() {
    UUID accountingEntityId = UUID.randomUUID();

    CreateAccountRequest request =
        new CreateAccountRequest(
            accountingEntityId,
            "1000",
            "Cash",
            AccountType.ASSET,
            NormalBalance.DEBIT,
            AccountClassification.CURRENT);

    when(accountingEntityRepository.findById(accountingEntityId)).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> service.create(request));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

    verify(accountingEntityRepository).findById(accountingEntityId);
    verify(accountRepository, never()).save(any(Account.class));
  }

  @Test
  void create_whenAccountCodeAlreadyExists_throwsConflict() {
    UUID accountingEntityId = UUID.randomUUID();

    AccountingEntity accountingEntity = new AccountingEntity();
    accountingEntity.setId(accountingEntityId);

    CreateAccountRequest request =
        new CreateAccountRequest(
            accountingEntityId,
            "1000",
            "Cash",
            AccountType.ASSET,
            NormalBalance.DEBIT,
            AccountClassification.CURRENT);

    when(accountingEntityRepository.findById(accountingEntityId))
        .thenReturn(Optional.of(accountingEntity));

    when(accountRepository.existsByAccountingEntity_IdAndCode(accountingEntityId, "1000"))
        .thenReturn(true);

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> service.create(request));

    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

    verify(accountRepository, never()).save(any(Account.class));
  }

  @Test
  void update_whenRequestIsValid_updatesAccountAndReturnsResponse() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();

    AccountingEntity accountingEntity = new AccountingEntity();
    accountingEntity.setId(accountingEntityId);

    Account existingAccount = new Account();
    existingAccount.setId(accountId);
    existingAccount.setAccountingEntity(accountingEntity);
    existingAccount.setCode("1000");
    existingAccount.setName("Cash");
    existingAccount.setAccountType(AccountType.ASSET);
    existingAccount.setNormalBalance(NormalBalance.DEBIT);
    existingAccount.setClassification(AccountClassification.CURRENT);
    existingAccount.setActive(true);

    UpdateAccountRequest request =
        new UpdateAccountRequest(
            "1010",
            "Checking Account",
            AccountType.ASSET,
            NormalBalance.DEBIT,
            AccountClassification.CURRENT,
            false);

    when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount));
    when(accountingEntityRepository.findById(accountingEntityId))
        .thenReturn(Optional.of(accountingEntity));
    when(accountRepository.findByAccountingEntity_IdAndCode(accountingEntityId, "1010"))
        .thenReturn(Optional.empty());
    when(accountRepository.save(existingAccount)).thenReturn(existingAccount);

    AccountResponse response = service.update(accountId, request);

    assertEquals(accountId, response.id());
    assertEquals(accountingEntityId, response.accountingEntityId());
    assertEquals("1010", response.code());
    assertEquals("Checking Account", response.name());
    assertEquals(AccountType.ASSET, response.accountType());
    assertEquals(NormalBalance.DEBIT, response.normalBalance());
    assertEquals(AccountClassification.CURRENT, response.classification());
    assertFalse(response.active());

    assertEquals("1010", existingAccount.getCode());
    assertEquals("Checking Account", existingAccount.getName());
    assertFalse(existingAccount.isActive());

    verify(accountRepository).save(existingAccount);
  }

  @Test
  void update_whenDuplicateAccountCodeBelongsToDifferentRecord_throwsConflict() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    final UUID duplicateAccountId = UUID.randomUUID();

    AccountingEntity accountingEntity = new AccountingEntity();
    accountingEntity.setId(accountingEntityId);

    Account existingAccount = new Account();
    existingAccount.setId(accountId);
    existingAccount.setAccountingEntity(accountingEntity);
    existingAccount.setCode("1000");

    Account duplicateAccount = new Account();
    duplicateAccount.setId(duplicateAccountId);
    duplicateAccount.setAccountingEntity(accountingEntity);
    duplicateAccount.setCode("1010");

    UpdateAccountRequest request =
        new UpdateAccountRequest(
            "1010",
            "Checking Account",
            AccountType.ASSET,
            NormalBalance.DEBIT,
            AccountClassification.CURRENT,
            true);

    when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount));
    when(accountingEntityRepository.findById(accountingEntityId))
        .thenReturn(Optional.of(accountingEntity));
    when(accountRepository.findByAccountingEntity_IdAndCode(accountingEntityId, "1010"))
        .thenReturn(Optional.of(duplicateAccount));

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> service.update(accountId, request));

    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

    verify(accountRepository, never()).save(any(Account.class));
  }

  @Test
  void update_whenMatchingAccountCodeBelongsToSameRecord_allowsUpdate() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();

    AccountingEntity accountingEntity = new AccountingEntity();
    accountingEntity.setId(accountingEntityId);

    Account existingAccount = new Account();
    existingAccount.setId(accountId);
    existingAccount.setAccountingEntity(accountingEntity);
    existingAccount.setCode("1000");
    existingAccount.setName("Cash");
    existingAccount.setAccountType(AccountType.ASSET);
    existingAccount.setNormalBalance(NormalBalance.DEBIT);
    existingAccount.setClassification(AccountClassification.CURRENT);
    existingAccount.setActive(true);

    UpdateAccountRequest request =
        new UpdateAccountRequest(
            "1000",
            "Cash Updated",
            AccountType.ASSET,
            NormalBalance.DEBIT,
            AccountClassification.CURRENT,
            true);

    when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount));
    when(accountingEntityRepository.findById(accountingEntityId))
        .thenReturn(Optional.of(accountingEntity));
    when(accountRepository.findByAccountingEntity_IdAndCode(accountingEntityId, "1000"))
        .thenReturn(Optional.of(existingAccount));
    when(accountRepository.save(existingAccount)).thenReturn(existingAccount);

    AccountResponse response = service.update(accountId, request);

    assertEquals(accountId, response.id());
    assertEquals("1000", response.code());
    assertEquals("Cash Updated", response.name());
    assertTrue(response.active());

    verify(accountRepository).save(existingAccount);
  }

  @Test
  void update_whenAccountDoesNotExist_throwsNotFound() {
    UUID accountId = UUID.randomUUID();
    UUID accountingEntityId = UUID.randomUUID();

    UpdateAccountRequest request =
        new UpdateAccountRequest(
            "1000",
            "Cash",
            AccountType.ASSET,
            NormalBalance.DEBIT,
            AccountClassification.CURRENT,
            true);

    when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> service.update(accountId, request));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

    verifyNoInteractions(accountingEntityRepository);
    verify(accountRepository, never()).save(any(Account.class));
  }

  @Test
  void findByAccountingEntity_returnsAccountsForAccountingEntity() {
    UUID accountingEntityId = UUID.randomUUID();

    AccountingEntity accountingEntity = new AccountingEntity();
    accountingEntity.setId(accountingEntityId);

    Account cash = new Account();
    cash.setId(UUID.randomUUID());
    cash.setAccountingEntity(accountingEntity);
    cash.setCode("1000");
    cash.setName("Cash");
    cash.setAccountType(AccountType.ASSET);
    cash.setNormalBalance(NormalBalance.DEBIT);
    cash.setClassification(AccountClassification.CURRENT);
    cash.setActive(true);

    Account revenue = new Account();
    revenue.setId(UUID.randomUUID());
    revenue.setAccountingEntity(accountingEntity);
    revenue.setCode("4000");
    revenue.setName("Service Revenue");
    revenue.setAccountType(AccountType.REVENUE);
    revenue.setNormalBalance(NormalBalance.CREDIT);
    revenue.setClassification(AccountClassification.NONE);
    revenue.setActive(true);

    when(accountRepository.findByAccountingEntity_IdOrderByCode(accountingEntityId))
        .thenReturn(List.of(cash, revenue));

    List<AccountResponse> responses = service.findByAccountingEntity(accountingEntityId);

    assertEquals(2, responses.size());

    assertEquals(cash.getId(), responses.get(0).id());
    assertEquals(accountingEntityId, responses.get(0).accountingEntityId());
    assertEquals("1000", responses.get(0).code());
    assertEquals("Cash", responses.get(0).name());
    assertEquals(AccountType.ASSET, responses.get(0).accountType());
    assertEquals(NormalBalance.DEBIT, responses.get(0).normalBalance());
    assertEquals(AccountClassification.CURRENT, responses.get(0).classification());
    assertTrue(responses.get(0).active());

    assertEquals(revenue.getId(), responses.get(1).id());
    assertEquals(accountingEntityId, responses.get(1).accountingEntityId());
    assertEquals("4000", responses.get(1).code());
    assertEquals("Service Revenue", responses.get(1).name());
    assertEquals(AccountType.REVENUE, responses.get(1).accountType());
    assertEquals(NormalBalance.CREDIT, responses.get(1).normalBalance());
    assertEquals(AccountClassification.NONE, responses.get(1).classification());
    assertTrue(responses.get(1).active());

    verify(accountRepository).findByAccountingEntity_IdOrderByCode(accountingEntityId);
  }

  @Test
  void delete_whenAccountExists_deactivatesAccountInsteadOfDeleting() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();

    AccountingEntity accountingEntity = new AccountingEntity();
    accountingEntity.setId(accountingEntityId);

    Account account = new Account();
    account.setId(accountId);
    account.setAccountingEntity(accountingEntity);
    account.setCode("1000");
    account.setName("Cash");
    account.setAccountType(AccountType.ASSET);
    account.setNormalBalance(NormalBalance.DEBIT);
    account.setClassification(AccountClassification.CURRENT);
    account.setActive(true);

    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
    when(accountRepository.save(account)).thenReturn(account);

    service.delete(accountId);

    assertFalse(account.isActive());

    verify(accountRepository).save(account);
    verify(accountRepository, never()).delete(any(Account.class));
  }

  @Test
  void create_whenAssetAccountHasCreditNormalBalance_throwsBadRequest() {
    UUID accountingEntityId = UUID.randomUUID();

    AccountingEntity accountingEntity = new AccountingEntity();
    accountingEntity.setId(accountingEntityId);

    CreateAccountRequest request =
        new CreateAccountRequest(
            accountingEntityId,
            "1000",
            "Cash",
            AccountType.ASSET,
            NormalBalance.CREDIT,
            AccountClassification.CURRENT);

    when(accountingEntityRepository.findById(accountingEntityId))
        .thenReturn(Optional.of(accountingEntity));

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> service.create(request));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

    verify(accountRepository, never()).save(any(Account.class));
  }

  @Test
  void create_whenRevenueAccountHasDebitNormalBalance_throwsBadRequest() {
    UUID accountingEntityId = UUID.randomUUID();

    AccountingEntity accountingEntity = new AccountingEntity();
    accountingEntity.setId(accountingEntityId);

    CreateAccountRequest request =
        new CreateAccountRequest(
            accountingEntityId,
            "4000",
            "Service Revenue",
            AccountType.REVENUE,
            NormalBalance.DEBIT,
            AccountClassification.NONE);

    when(accountingEntityRepository.findById(accountingEntityId))
        .thenReturn(Optional.of(accountingEntity));

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> service.create(request));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

    verify(accountRepository, never()).save(any(Account.class));
  }
}
