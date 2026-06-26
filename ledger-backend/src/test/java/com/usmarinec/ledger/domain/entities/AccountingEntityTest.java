package com.usmarinec.ledger.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountingEntityTest {

  @Autowired private EntityManager entityManager;

  @Test
  void persistsAccountingEntityWithGeneratedIdAndCreatedAt() {
    final LocalDateTime beforePersist = LocalDateTime.now();

    AccountingEntity accountingEntity = new AccountingEntity();
    accountingEntity.setName("Test Entity");

    entityManager.persist(accountingEntity);
    entityManager.flush();

    LocalDateTime afterPersist = LocalDateTime.now();

    assertThat(accountingEntity.getId()).isNotNull();
    assertThat(accountingEntity.getCreatedAt()).isNotNull();
    assertThat(accountingEntity.getCreatedAt()).isBetween(beforePersist, afterPersist);
  }

  @Test
  void persistsAccountingEntityAndCanReadItBack() {
    AccountingEntity accountingEntity = new AccountingEntity();
    accountingEntity.setName("Test Entity");

    entityManager.persist(accountingEntity);
    entityManager.flush();

    UUID id = accountingEntity.getId();

    entityManager.clear();

    AccountingEntity found = entityManager.find(AccountingEntity.class, id);

    assertThat(found).isNotNull();
    assertThat(found.getId()).isEqualTo(id);
    assertThat(found.getName()).isEqualTo("Test Entity");
    assertThat(found.getCreatedAt()).isNotNull();
  }

  @Test
  void doesNotOverwriteCreatedAtWhenAlreadySet() {
    LocalDateTime originalCreatedAt = LocalDateTime.of(2026, 1, 1, 12, 30);

    AccountingEntity accountingEntity = new AccountingEntity();
    accountingEntity.setName("Test Entity");
    accountingEntity.setCreatedAt(originalCreatedAt);

    entityManager.persist(accountingEntity);
    entityManager.flush();

    assertThat(accountingEntity.getId()).isNotNull();
    assertThat(accountingEntity.getCreatedAt()).isEqualTo(originalCreatedAt);
  }
}
