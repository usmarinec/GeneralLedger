package com.usmarinec.ledger.services.fiscal;

import com.usmarinec.ledger.domain.entities.AccountingEntity;
import com.usmarinec.ledger.domain.fiscal.FiscalYear;
import com.usmarinec.ledger.dto.fiscal.CreateFiscalYearRequest;
import com.usmarinec.ledger.dto.fiscal.FiscalYearResponse;
import com.usmarinec.ledger.dto.fiscal.UpdateFiscalYearRequest;
import com.usmarinec.ledger.exception.exceptions.BadRequestException;
import com.usmarinec.ledger.exception.exceptions.ConflictException;
import com.usmarinec.ledger.exception.exceptions.NotFoundException;
import com.usmarinec.ledger.repositories.entities.AccountingEntityRepository;
import com.usmarinec.ledger.repositories.fiscal.FiscalYearRepository;
import com.usmarinec.ledger.services.LedgerService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FiscalYearService
    extends LedgerService<
        FiscalYear,
        FiscalYearRepository,
        CreateFiscalYearRequest,
        UpdateFiscalYearRequest,
        FiscalYearResponse> {
  @Autowired AccountingEntityRepository accountingEntityRepository;

  @Override
  protected FiscalYear createLedgerEntity(CreateFiscalYearRequest request) {
    this.validateDateRange(request.startDate(), request.endDate());
    AccountingEntity accountingEntity = this.getAccountingEntity(request.accountingEntityId());

    if (this.repository.existsByAccountingEntity_IdAndYear(
        request.accountingEntityId(), request.year())) {
      throw new ConflictException("Fiscal year already exists for this accounting entity");
    }

    FiscalYear fiscalYear = new FiscalYear();
    fiscalYear.setAccountingEntity(accountingEntity);
    fiscalYear.setYear(request.year());
    fiscalYear.setStartDate(request.startDate());
    fiscalYear.setEndDate(request.endDate());
    fiscalYear.setClosed(false);
    return fiscalYear;
  }

  @Override
  protected void updateLedgerEntity(FiscalYear ledgerEntity, UpdateFiscalYearRequest request) {
    this.validateDateRange(request.startDate(), request.endDate());

    AccountingEntity accountingEntity = this.getAccountingEntity(request.accountingEntityId());
    this.repository
        .findByAccountingEntity_IdAndYear(request.accountingEntityId(), request.year())
        .filter(existing -> !existing.getId().equals(ledgerEntity.getId()))
        .ifPresent(
            existing -> {
              throw new ConflictException("Fiscal year already exists for this accounting entity");
            });
    ledgerEntity.setAccountingEntity(accountingEntity);
    ledgerEntity.setYear(request.year());
    ledgerEntity.setStartDate(request.startDate());
    ledgerEntity.setEndDate(request.endDate());
    ledgerEntity.setClosed(request.closed());
  }

  @Override
  protected FiscalYearResponse toResponse(FiscalYear ledgerEntity) {
    return new FiscalYearResponse(
        ledgerEntity.getId(),
        ledgerEntity.getAccountingEntity().getId(),
        ledgerEntity.getYear(),
        ledgerEntity.getStartDate(),
        ledgerEntity.getEndDate(),
        ledgerEntity.isClosed());
  }

  /**
   * Returns a list of FiscalYears for a given AccountingEntity Id.
   *
   * @param accountingEntityId UUID @Return List<FiscalYearResponse> response record
   */
  @Transactional(readOnly = true)
  public List<FiscalYearResponse> findByAccountingEntity(UUID accountingEntityId) {
    return this.repository.findByAccountingEntity_IdOrderByYearDesc(accountingEntityId).stream()
        .map(this::toResponse)
        .toList();
  }

  private AccountingEntity getAccountingEntity(UUID accountingEntityId) {
    return this.accountingEntityRepository
        .findById(accountingEntityId)
        .orElseThrow(() -> new NotFoundException("Accounting entity not found"));
  }

  private void validateDateRange(LocalDate startDate, LocalDate endDate) {
    if (startDate.isAfter(endDate)) {
      throw new BadRequestException("Fiscal year start date must be before or equal to end date");
    }
  }
}
