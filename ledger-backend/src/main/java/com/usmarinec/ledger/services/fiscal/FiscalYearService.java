package com.usmarinec.ledger.services.fiscal;

import com.usmarinec.ledger.domain.entities.AccountingEntity;
import com.usmarinec.ledger.domain.fiscal.FiscalYear;
import com.usmarinec.ledger.dto.fiscal.CreateFiscalYearRequest;
import com.usmarinec.ledger.dto.fiscal.FiscalYearResponse;
import com.usmarinec.ledger.dto.fiscal.UpdateFiscalYearRequest;
import com.usmarinec.ledger.repositories.entities.AccountingEntityRepository;
import com.usmarinec.ledger.repositories.fiscal.FiscalYearRepository;
import com.usmarinec.ledger.services.LedgerService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Fiscal year aleady exists for this accounting entity");
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
              throw new ResponseStatusException(
                  HttpStatus.CONFLICT, "Fiscal year already exists for this accounting entity");
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
  public List<FiscalYearResponse> findByAccountingEntity(UUID accountingEntityId) {
    return this.repository.findByAccountingEntity_IdOrderByYearDesc(accountingEntityId).stream()
        .map(this::toResponse)
        .toList();
  }

  private AccountingEntity getAccountingEntity(UUID accountingEntityId) {
    return this.accountingEntityRepository
        .findById(accountingEntityId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Accounting entity not found"));
  }

  private void validateDateRange(LocalDate startDate, LocalDate endDate) {
    if (startDate.isAfter(endDate)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Fiscal year start date must be before or equal to end date");
    }
  }
}
