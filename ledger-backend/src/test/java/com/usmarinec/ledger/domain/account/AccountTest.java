package com.usmarinec.ledger.domain.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.usmarinec.ledger.domain.entities.AccountingEntity;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountTest {

  @Autowired private EntityManager entityManager;

  @Test
  void persistsAccountWithGeneratedId() {
    AccountingEntity entity = createAccountingEntity("Test Entity");

    Account account = new Account();
    account.setAccountingEntity(entity);
    account.setCode("1000");
    account.setName("Cash");
    account.setAccountType(AccountType.ASSET);
    account.setNormalBalance(NormalBalance.DEBIT);
    account.setClassification(AccountClassification.CURRENT);

    entityManager.persist(account);
    entityManager.flush();

    assertThat(account.getId()).isNotNull();
    assertThat(account.isActive()).isTrue();
    assertThat(account.getClassification()).isEqualTo(AccountClassification.CURRENT);
  }

  @Test
  void persistsAccountAndCanReadItBack() {
    AccountingEntity entity = createAccountingEntity("Test Entity");

    Account account =
        createAccount(
            entity,
            "1000",
            "Cash",
            AccountType.ASSET,
            NormalBalance.DEBIT,
            AccountClassification.CURRENT);

    UUID accountId = account.getId();

    entityManager.clear();

    Account found = entityManager.find(Account.class, accountId);

    assertThat(found).isNotNull();
    assertThat(found.getId()).isEqualTo(accountId);
    assertThat(found.getCode()).isEqualTo("1000");
    assertThat(found.getName()).isEqualTo("Cash");
    assertThat(found.getAccountType()).isEqualTo(AccountType.ASSET);
    assertThat(found.getNormalBalance()).isEqualTo(NormalBalance.DEBIT);
    assertThat(found.getClassification()).isEqualTo(AccountClassification.CURRENT);
    assertThat(found.isActive()).isTrue();
    assertThat(found.getAccountingEntity().getId()).isEqualTo(entity.getId());
  }

  @Test
  void defaultsClassificationToNoneAndActiveToTrue() {
    AccountingEntity entity = createAccountingEntity("Test Entity");

    Account account = new Account();
    account.setAccountingEntity(entity);
    account.setCode("2000");
    account.setName("Accounts Payable");
    account.setAccountType(AccountType.LIABILITY);
    account.setNormalBalance(NormalBalance.CREDIT);

    entityManager.persist(account);
    entityManager.flush();

    assertThat(account.getId()).isNotNull();
    assertThat(account.getClassification()).isEqualTo(AccountClassification.NONE);
    assertThat(account.isActive()).isTrue();
  }

  @Test
  void doesNotAllowDuplicateAccountCodeForSameAccountingEntity() {
    AccountingEntity entity = createAccountingEntity("Test Entity");

    createAccount(
        entity,
        "1000",
        "Cash",
        AccountType.ASSET,
        NormalBalance.DEBIT,
        AccountClassification.CURRENT);

    Account duplicate = new Account();
    duplicate.setAccountingEntity(entity);
    duplicate.setCode("1000");
    duplicate.setName("Duplicate Cash");
    duplicate.setAccountType(AccountType.ASSET);
    duplicate.setNormalBalance(NormalBalance.DEBIT);
    duplicate.setClassification(AccountClassification.CURRENT);

    entityManager.persist(duplicate);

    assertThatThrownBy(() -> entityManager.flush()).isInstanceOf(RuntimeException.class);
  }

  @Test
  void allowsSameAccountCodeForDifferentAccountingEntities() {
    AccountingEntity firstEntity = createAccountingEntity("First Entity");
    AccountingEntity secondEntity = createAccountingEntity("Second Entity");

    Account firstAccount =
        createAccount(
            firstEntity,
            "1000",
            "Cash",
            AccountType.ASSET,
            NormalBalance.DEBIT,
            AccountClassification.CURRENT);

    Account secondAccount =
        createAccount(
            secondEntity,
            "1000",
            "Cash",
            AccountType.ASSET,
            NormalBalance.DEBIT,
            AccountClassification.CURRENT);

    entityManager.flush();

    assertThat(firstAccount.getId()).isNotNull();
    assertThat(secondAccount.getId()).isNotNull();
    assertThat(firstAccount.getCode()).isEqualTo(secondAccount.getCode());
    assertThat(firstAccount.getAccountingEntity().getId())
        .isNotEqualTo(secondAccount.getAccountingEntity().getId());
  }

  private AccountingEntity createAccountingEntity(String name) {
    AccountingEntity entity = new AccountingEntity();
    entity.setName(name);

    entityManager.persist(entity);
    entityManager.flush();

    return entity;
  }

  private Account createAccount(
      AccountingEntity entity,
      String code,
      String name,
      AccountType accountType,
      NormalBalance normalBalance,
      AccountClassification classification) {
    Account account = new Account();
    account.setAccountingEntity(entity);
    account.setCode(code);
    account.setName(name);
    account.setAccountType(accountType);
    account.setNormalBalance(normalBalance);
    account.setClassification(classification);

    entityManager.persist(account);
    entityManager.flush();

    return account;
  }
}
