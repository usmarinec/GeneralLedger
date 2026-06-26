package com.usmarinec.ledger.domain.fiscal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.usmarinec.ledger.domain.entities.AccountingEntity;
import jakarta.persistence.EntityManager;
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
class FiscalYearTest {

  @Autowired private EntityManager entityManager;

  @Test
  void persistsFiscalYearWithGeneratedId() {
    AccountingEntity entity = createAccountingEntity("Test Entity");

    FiscalYear fiscalYear = new FiscalYear();
    fiscalYear.setAccountingEntity(entity);
    fiscalYear.setYear(2026);
    fiscalYear.setStartDate(LocalDate.of(2026, 1, 1));
    fiscalYear.setEndDate(LocalDate.of(2026, 12, 31));

    entityManager.persist(fiscalYear);
    entityManager.flush();

    assertThat(fiscalYear.getId()).isNotNull();
    assertThat(fiscalYear.isClosed()).isFalse();
  }

  @Test
  void persistsFiscalYearAndCanReadItBack() {
    AccountingEntity entity = createAccountingEntity("Test Entity");

    FiscalYear fiscalYear =
        createFiscalYear(entity, 2026, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

    UUID fiscalYearId = fiscalYear.getId();

    entityManager.clear();

    FiscalYear found = entityManager.find(FiscalYear.class, fiscalYearId);

    assertThat(found).isNotNull();
    assertThat(found.getId()).isEqualTo(fiscalYearId);
    assertThat(found.getAccountingEntity().getId()).isEqualTo(entity.getId());
    assertThat(found.getYear()).isEqualTo(2026);
    assertThat(found.getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
    assertThat(found.getEndDate()).isEqualTo(LocalDate.of(2026, 12, 31));
    assertThat(found.isClosed()).isFalse();
  }

  @Test
  void defaultsClosedToFalse() {
    AccountingEntity entity = createAccountingEntity("Test Entity");

    FiscalYear fiscalYear = new FiscalYear();
    fiscalYear.setAccountingEntity(entity);
    fiscalYear.setYear(2026);
    fiscalYear.setStartDate(LocalDate.of(2026, 1, 1));
    fiscalYear.setEndDate(LocalDate.of(2026, 12, 31));

    entityManager.persist(fiscalYear);
    entityManager.flush();

    assertThat(fiscalYear.getId()).isNotNull();
    assertThat(fiscalYear.isClosed()).isFalse();
  }

  @Test
  void doesNotAllowDuplicateFiscalYearForSameAccountingEntity() {
    AccountingEntity entity = createAccountingEntity("Test Entity");

    createFiscalYear(entity, 2026, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

    FiscalYear duplicate = new FiscalYear();
    duplicate.setAccountingEntity(entity);
    duplicate.setYear(2026);
    duplicate.setStartDate(LocalDate.of(2026, 1, 1));
    duplicate.setEndDate(LocalDate.of(2026, 12, 31));

    entityManager.persist(duplicate);

    assertThatThrownBy(() -> entityManager.flush()).isInstanceOf(RuntimeException.class);
  }

  @Test
  void allowsSameFiscalYearForDifferentAccountingEntities() {
    AccountingEntity firstEntity = createAccountingEntity("First Entity");
    AccountingEntity secondEntity = createAccountingEntity("Second Entity");

    FiscalYear firstFiscalYear =
        createFiscalYear(firstEntity, 2026, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

    FiscalYear secondFiscalYear =
        createFiscalYear(secondEntity, 2026, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

    assertThat(firstFiscalYear.getId()).isNotNull();
    assertThat(secondFiscalYear.getId()).isNotNull();
    assertThat(firstFiscalYear.getYear()).isEqualTo(secondFiscalYear.getYear());
    assertThat(firstFiscalYear.getAccountingEntity().getId())
        .isNotEqualTo(secondFiscalYear.getAccountingEntity().getId());
  }

  @Test
  void persistsClosedFiscalYear() {
    AccountingEntity entity = createAccountingEntity("Test Entity");

    FiscalYear fiscalYear = new FiscalYear();
    fiscalYear.setAccountingEntity(entity);
    fiscalYear.setYear(2026);
    fiscalYear.setStartDate(LocalDate.of(2026, 1, 1));
    fiscalYear.setEndDate(LocalDate.of(2026, 12, 31));
    fiscalYear.setClosed(true);

    entityManager.persist(fiscalYear);
    entityManager.flush();

    UUID fiscalYearId = fiscalYear.getId();

    entityManager.clear();

    FiscalYear found = entityManager.find(FiscalYear.class, fiscalYearId);

    assertThat(found).isNotNull();
    assertThat(found.isClosed()).isTrue();
  }

  private AccountingEntity createAccountingEntity(String name) {
    AccountingEntity entity = new AccountingEntity();
    entity.setName(name);

    entityManager.persist(entity);
    entityManager.flush();

    return entity;
  }

  private FiscalYear createFiscalYear(
      AccountingEntity entity, Integer year, LocalDate startDate, LocalDate endDate) {
    FiscalYear fiscalYear = new FiscalYear();
    fiscalYear.setAccountingEntity(entity);
    fiscalYear.setYear(year);
    fiscalYear.setStartDate(startDate);
    fiscalYear.setEndDate(endDate);

    entityManager.persist(fiscalYear);
    entityManager.flush();

    return fiscalYear;
  }
}
