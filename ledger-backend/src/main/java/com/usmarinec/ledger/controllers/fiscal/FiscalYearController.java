package com.usmarinec.ledger.controllers.fiscal;

import com.usmarinec.ledger.controllers.LedgerController;
import com.usmarinec.ledger.domain.fiscal.FiscalYear;
import com.usmarinec.ledger.dto.fiscal.CreateFiscalYearRequest;
import com.usmarinec.ledger.dto.fiscal.FiscalYearResponse;
import com.usmarinec.ledger.dto.fiscal.UpdateFiscalYearRequest;
import com.usmarinec.ledger.repositories.fiscal.FiscalYearRepository;
import com.usmarinec.ledger.responses.SuccessFailureResponse;
import com.usmarinec.ledger.responses.SuccessFailureResponseUtility;
import com.usmarinec.ledger.services.fiscal.FiscalYearService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/FiscalYear")
public class FiscalYearController
    extends LedgerController<
        FiscalYear,
        FiscalYearRepository,
        CreateFiscalYearRequest,
        UpdateFiscalYearRequest,
        FiscalYearResponse,
        FiscalYearService> {

  /**
   * Fetches all FiscalYears by AccountingEntity ID.
   *
   * @param accountingEntityId id to search
   * @return SuccessFailureResponse with List of FiscalYears
   */
  @GetMapping("/fetch-by-accounting-entity/{accountingEntityId}")
  public ResponseEntity<SuccessFailureResponse<FiscalYearResponse>> fetchByAccountingEntityId(
      @PathVariable UUID accountingEntityId) {
    List<FiscalYearResponse> fiscalYearsList =
        this.service.findByAccountingEntity(accountingEntityId);
    return SuccessFailureResponseUtility.createSuccessFailureResponse(
        true, "Record(s) found", HttpStatus.OK, fiscalYearsList);
  }
}
