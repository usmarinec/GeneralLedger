package com.usmarinec.ledger.domain.journal;

import static org.assertj.core.api.Assertions.assertThat;

import com.usmarinec.ledger.domain.entities.AccountingEntity;
import com.usmarinec.ledger.domain.fiscal.FiscalYear;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
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
class JournalEntryTest {

  @Autowired private EntityManager entityManager;

  @Test
  void persistsJournalEntryWithGeneratedIdAndCreatedAt() {
    AccountingEntity entity = createAccountingEntity("Test Entity");
    FiscalYear fiscalYear = createFiscalYear(entity, 2026);

    final LocalDateTime beforePersist = LocalDateTime.now();

    JournalEntry journalEntry = new JournalEntry();
    journalEntry.setAccountingEntity(entity);
    journalEntry.setFiscalYear(fiscalYear);
    journalEntry.setEntryDate(LocalDate.of(2026, 1, 15));
    journalEntry.setMemo("Opening journal entry");

    entityManager.persist(journalEntry);
    entityManager.flush();

    LocalDateTime afterPersist = LocalDateTime.now();

    assertThat(journalEntry.getId()).isNotNull();
    assertThat(journalEntry.getCreatedAt()).isNotNull();
    assertThat(journalEntry.getCreatedAt()).isBetween(beforePersist, afterPersist);
  }

  @Test
  void defaultsEntryTypeStatusAndLines() {
    JournalEntry journalEntry = new JournalEntry();

    assertThat(journalEntry.getEntryType()).isEqualTo(JournalEntryType.STANDARD);
    assertThat(journalEntry.getStatus()).isEqualTo(JournalEntryStatus.DRAFT);
    assertThat(journalEntry.getLines()).isNotNull();
    assertThat(journalEntry.getLines()).isEmpty();
  }

  @Test
  void persistsJournalEntryAndCanReadItBack() {
    AccountingEntity entity = createAccountingEntity("Test Entity");
    FiscalYear fiscalYear = createFiscalYear(entity, 2026);

    JournalEntry journalEntry = new JournalEntry();
    journalEntry.setAccountingEntity(entity);
    journalEntry.setFiscalYear(fiscalYear);
    journalEntry.setEntryDate(LocalDate.of(2026, 1, 15));
    journalEntry.setEntryType(JournalEntryType.ADJUSTING);
    journalEntry.setStatus(JournalEntryStatus.POSTED);
    journalEntry.setMemo("Adjusting entry");
    journalEntry.setPostedAt(LocalDateTime.of(2026, 1, 16, 10, 30));

    entityManager.persist(journalEntry);
    entityManager.flush();

    UUID journalEntryId = journalEntry.getId();

    entityManager.clear();

    JournalEntry found = entityManager.find(JournalEntry.class, journalEntryId);

    assertThat(found).isNotNull();
    assertThat(found.getId()).isEqualTo(journalEntryId);
    assertThat(found.getAccountingEntity().getId()).isEqualTo(entity.getId());
    assertThat(found.getFiscalYear().getId()).isEqualTo(fiscalYear.getId());
    assertThat(found.getEntryDate()).isEqualTo(LocalDate.of(2026, 1, 15));
    assertThat(found.getEntryType()).isEqualTo(JournalEntryType.ADJUSTING);
    assertThat(found.getStatus()).isEqualTo(JournalEntryStatus.POSTED);
    assertThat(found.getMemo()).isEqualTo("Adjusting entry");
    assertThat(found.getCreatedAt()).isNotNull();
    assertThat(found.getPostedAt()).isEqualTo(LocalDateTime.of(2026, 1, 16, 10, 30));
  }

  @Test
  void doesNotOverwriteCreatedAtWhenAlreadySet() {
    AccountingEntity entity = createAccountingEntity("Test Entity");
    FiscalYear fiscalYear = createFiscalYear(entity, 2026);
    LocalDateTime originalCreatedAt = LocalDateTime.of(2026, 1, 1, 12, 30);

    JournalEntry journalEntry = new JournalEntry();
    journalEntry.setAccountingEntity(entity);
    journalEntry.setFiscalYear(fiscalYear);
    journalEntry.setEntryDate(LocalDate.of(2026, 1, 15));
    journalEntry.setCreatedAt(originalCreatedAt);

    entityManager.persist(journalEntry);
    entityManager.flush();

    assertThat(journalEntry.getId()).isNotNull();
    assertThat(journalEntry.getCreatedAt()).isEqualTo(originalCreatedAt);
  }

  @Test
  void addLineAddsLineAndSetsJournalEntryOnLine() {
    JournalEntry journalEntry = new JournalEntry();
    JournalEntryLine line = new JournalEntryLine();

    journalEntry.addLine(line);

    assertThat(journalEntry.getLines()).containsExactly(line);
    assertThat(line.getJournalEntry()).isEqualTo(journalEntry);
  }

  @Test
  void removeLineRemovesLineAndClearsJournalEntryOnLine() {
    JournalEntry journalEntry = new JournalEntry();
    JournalEntryLine line = new JournalEntryLine();

    journalEntry.addLine(line);
    journalEntry.removeLine(line);

    assertThat(journalEntry.getLines()).doesNotContain(line);
    assertThat(line.getJournalEntry()).isNull();
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
}
