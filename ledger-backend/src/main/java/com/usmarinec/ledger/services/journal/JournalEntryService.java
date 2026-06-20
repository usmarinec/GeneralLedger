package com.usmarinec.ledger.services.journal;

import com.usmarinec.ledger.domain.account.Account;
import com.usmarinec.ledger.domain.entities.AccountingEntity;
import com.usmarinec.ledger.domain.fiscal.FiscalYear;
import com.usmarinec.ledger.domain.journal.JournalEntry;
import com.usmarinec.ledger.domain.journal.JournalEntryLine;
import com.usmarinec.ledger.domain.journal.JournalEntryStatus;
import com.usmarinec.ledger.dto.journal.entry.CreateJournalEntryRequest;
import com.usmarinec.ledger.dto.journal.entry.JournalEntryResponse;
import com.usmarinec.ledger.dto.journal.entry.UpdateJournalEntryRequest;
import com.usmarinec.ledger.dto.journal.line.CreateJournalEntryLineRequest;
import com.usmarinec.ledger.dto.journal.line.JournalEntryLineRequest;
import com.usmarinec.ledger.dto.journal.line.JournalEntryLineResponse;
import com.usmarinec.ledger.dto.journal.line.UpdateJournalEntryLineRequest;
import com.usmarinec.ledger.repositories.account.AccountRepository;
import com.usmarinec.ledger.repositories.entities.AccountingEntityRepository;
import com.usmarinec.ledger.repositories.fiscal.FiscalYearRepository;
import com.usmarinec.ledger.repositories.journal.JournalEntryRepository;
import com.usmarinec.ledger.services.LedgerService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class JournalEntryService
    extends LedgerService<
        JournalEntry,
        JournalEntryRepository,
        CreateJournalEntryRequest,
        UpdateJournalEntryRequest,
        JournalEntryResponse> {
  @Autowired AccountingEntityRepository accountingEntityRepository;
  @Autowired FiscalYearRepository fiscalYearRepository;
  @Autowired AccountRepository accountRepository;

  /**
   * Retrieves all JournalEntries for an AccountingEntity in a given FiscalYear.
   *
   * @param accountingEntityId UUID of AccountingEntity
   * @param fiscalYearId UUID of FiscalYear
   * @return List<JournalEntryResponse> of all entries matching inputs
   */
  @Transactional(readOnly = true)
  public List<JournalEntryResponse> findByAccountingEntityAndFiscalYear(
      UUID accountingEntityId, UUID fiscalYearId) {
    return this.repository
        .findByAccountingEntity_IdAndFiscalYear_IdOrderByEntryDateAsc(
            accountingEntityId, fiscalYearId)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  /**
   * Posts the JournalEntry and sets the status to POSTED.
   *
   * @param id UUID of JournalEntry to post
   * @return JournalEntryResponse of updated entry
   */
  @Transactional
  public JournalEntryResponse post(UUID id) {
    JournalEntry journalEntry = this.getLedgerEntity(id);
    if (journalEntry.getStatus() == JournalEntryStatus.POSTED) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JournalEntry already posted");
    }
    if (journalEntry.getStatus() == JournalEntryStatus.VOIDED) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot post a voided entry");
    }
    if (journalEntry.getFiscalYear().isClosed()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Cannot post to a closed fiscal year");
    }
    this.validateBalancedFromEntity(journalEntry);
    journalEntry.setStatus(JournalEntryStatus.POSTED);
    journalEntry.setPostedAt(LocalDateTime.now());

    JournalEntry saved = this.repository.save(journalEntry);

    return this.toResponse(saved);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    JournalEntry journalEntry = this.getLedgerEntity(id);
    journalEntry.setStatus(JournalEntryStatus.VOIDED);
    this.repository.save(journalEntry);
  }

  @Override
  protected JournalEntry getLedgerEntity(UUID id) {
    return this.repository
        .findWithLinesById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal entry not found"));
  }

  @Override
  protected JournalEntry createLedgerEntity(CreateJournalEntryRequest request) {
    AccountingEntity accountingEntity = this.getAccountingEntity(request.accountingEntityId());
    FiscalYear fiscalYear = this.getFiscalYear(request.fiscalYearId());

    this.validateFiscalYearBelongsToAccountingEntity(fiscalYear, accountingEntity);
    this.validateFiscalYearIsOpen(fiscalYear);
    this.validateEntryDateWithinFiscalYear(request.entryDate(), fiscalYear);
    this.validateLineCount(request.lines());
    this.validateBalanced(request.lines());

    JournalEntry journalEntry = new JournalEntry();
    journalEntry.setAccountingEntity(accountingEntity);
    journalEntry.setFiscalYear(fiscalYear);
    journalEntry.setEntryDate(request.entryDate());
    journalEntry.setEntryType(request.entryType());
    journalEntry.setStatus(JournalEntryStatus.DRAFT);
    journalEntry.setMemo(request.memo());

    int lineNumber = 1;

    for (CreateJournalEntryLineRequest lineRequest : request.lines()) {
      Account account =
          this.accountRepository
              .findById(lineRequest.accountId())
              .orElseThrow(
                  () ->
                      new ResponseStatusException(
                          HttpStatus.NOT_FOUND, "Account not found: " + lineRequest.accountId()));
      if (!account.getAccountingEntity().getId().equals(accountingEntity.getId())) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Account does not belong to AccountingEntity: " + account.getId());
      }
      if (!account.isActive()) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Cannot use inactive account: " + account.getCode());
      }
      JournalEntryLine line = this.createLine(lineRequest, accountingEntity, lineNumber++);
      journalEntry.addLine(line);
    }
    return journalEntry;
  }

  @Override
  protected void updateLedgerEntity(JournalEntry journalEntry, UpdateJournalEntryRequest request) {
    if (journalEntry.getStatus() == JournalEntryStatus.POSTED) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Cannot update a posted journal entry");
    }

    if (journalEntry.getStatus() == JournalEntryStatus.VOIDED) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Cannot update a voided journal entry");
    }

    AccountingEntity accountingEntity = journalEntry.getAccountingEntity();
    FiscalYear fiscalYear = getFiscalYear(request.fiscalYearId());

    validateFiscalYearBelongsToAccountingEntity(fiscalYear, accountingEntity);
    validateFiscalYearIsOpen(fiscalYear);
    validateEntryDateWithinFiscalYear(request.entryDate(), fiscalYear);
    validateLineCount(request.lines());
    validateBalanced(request.lines());

    journalEntry.setFiscalYear(fiscalYear);
    journalEntry.setEntryDate(request.entryDate());
    journalEntry.setEntryType(request.entryType());
    journalEntry.setMemo(request.memo());

    journalEntry.clearLines();

    this.repository.flush();

    int lineNumber = 1;

    for (UpdateJournalEntryLineRequest lineRequest : request.lines()) {
      JournalEntryLine line = createLine(lineRequest, accountingEntity, lineNumber++);
      journalEntry.addLine(line);
    }
  }

  @Override
  protected JournalEntryResponse toResponse(JournalEntry ledgerEntity) {
    return new JournalEntryResponse(
        ledgerEntity.getId(),
        ledgerEntity.getAccountingEntity().getId(),
        ledgerEntity.getFiscalYear().getId(),
        ledgerEntity.getEntryDate(),
        ledgerEntity.getEntryType(),
        ledgerEntity.getStatus(),
        ledgerEntity.getMemo(),
        ledgerEntity.getCreatedAt(),
        ledgerEntity.getPostedAt(),
        ledgerEntity.getLines().stream()
            .sorted((a, b) -> a.getLineNumber().compareTo(b.getLineNumber()))
            .map(this::toLineResponse)
            .toList());
  }

  private JournalEntryLineResponse toLineResponse(JournalEntryLine line) {
    return new JournalEntryLineResponse(
        line.getId(),
        line.getLineNumber(),
        line.getAccount().getId(),
        line.getAccount().getCode(),
        line.getAccount().getName(),
        line.getDescription(),
        line.getDebitAmount(),
        line.getCreditAmount());
  }

  private void validateEntryDateWithinFiscalYear(LocalDate entryDate, FiscalYear fiscalYear) {
    if (entryDate.isBefore(fiscalYear.getStartDate())
        || entryDate.isAfter(fiscalYear.getEndDate())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Journal entry date must be within the fiscal year");
    }
  }

  private void validateLineCount(List<? extends JournalEntryLineRequest> lines) {
    if (lines == null || lines.size() < 2) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Journal entry must have at least two lines");
    }
  }

  private void validateBalanced(List<? extends JournalEntryLineRequest> lines) {
    BigDecimal totalDebits = BigDecimal.ZERO;
    BigDecimal totalCredits = BigDecimal.ZERO;

    for (JournalEntryLineRequest line : lines) {
      BigDecimal debit = zeroIfNull(line.debitAmount());
      BigDecimal credit = zeroIfNull(line.creditAmount());

      validateLineDebitCredit(debit, credit);

      totalDebits = totalDebits.add(debit);
      totalCredits = totalCredits.add(credit);
    }

    if (totalDebits.compareTo(totalCredits) != 0) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Journal entry is not balanced. Debits must equal credits.");
    }
  }

  private void validateBalancedFromEntity(JournalEntry journalEntry) {
    BigDecimal totalDebits = BigDecimal.ZERO;
    BigDecimal totalCredits = BigDecimal.ZERO;

    for (JournalEntryLine line : journalEntry.getLines()) {
      BigDecimal debit = zeroIfNull(line.getDebitAmount());
      BigDecimal credit = zeroIfNull(line.getCreditAmount());

      this.validateLineDebitCredit(debit, credit);

      totalDebits = totalDebits.add(debit);
      totalCredits = totalCredits.add(credit);
    }

    if (totalDebits.compareTo(totalCredits) != 0) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Journal entry is not balanced. Debits must equal credits.");
    }
  }

  private void validateLineDebitCredit(BigDecimal debit, BigDecimal credit) {
    boolean hasDebit = debit.compareTo(BigDecimal.ZERO) > 0;
    boolean hasCredit = credit.compareTo(BigDecimal.ZERO) > 0;

    if (hasDebit == hasCredit) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Each journal line must have either a debit or a credit, but not both");
    }
  }

  private BigDecimal zeroIfNull(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  private AccountingEntity getAccountingEntity(UUID accountingEntityId) {
    return this.accountingEntityRepository
        .findById(accountingEntityId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Accounting entity not found"));
  }

  private FiscalYear getFiscalYear(UUID fiscalYearId) {
    return this.fiscalYearRepository
        .findById(fiscalYearId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fiscal year not found"));
  }

  private void validateFiscalYearBelongsToAccountingEntity(
      FiscalYear fiscalYear, AccountingEntity accountingEntity) {
    if (!fiscalYear.getAccountingEntity().getId().equals(accountingEntity.getId())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Fiscal year does not belong to accounting entity");
    }
  }

  private void validateFiscalYearIsOpen(FiscalYear fiscalYear) {
    if (fiscalYear.isClosed()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot use a closed fiscal year");
    }
  }

  private JournalEntryLine createLine(
      JournalEntryLineRequest lineRequest, AccountingEntity accountingEntity, int lineNumber) {
    Account account =
        this.accountRepository
            .findById(lineRequest.accountId())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Account not found: " + lineRequest.accountId()));

    if (!account.getAccountingEntity().getId().equals(accountingEntity.getId())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Account does not belong to accounting entity: " + account.getId());
    }

    if (!account.isActive()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Cannot use inactive account: " + account.getCode());
    }

    JournalEntryLine line = new JournalEntryLine();
    line.setAccount(account);
    line.setLineNumber(lineNumber);
    line.setDescription(lineRequest.description());
    line.setDebitAmount(zeroIfNull(lineRequest.debitAmount()));
    line.setCreditAmount(zeroIfNull(lineRequest.creditAmount()));

    return line;
  }
}
