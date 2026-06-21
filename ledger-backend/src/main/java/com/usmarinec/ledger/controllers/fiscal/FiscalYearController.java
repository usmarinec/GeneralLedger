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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
    name = "Fiscal Years",
    description =
        "Manage fiscal years for an accounting entity, including open and closed fiscal periods.")
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
  @Operation(
      summary = "Fetch fiscal years by accounting entity",
      description = "Returns all fiscal years belonging to the specified accounting entity.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Fiscal years found"),
    @ApiResponse(
        responseCode = "404",
        description = "Accounting entity not found",
        content = @Content)
  })
  @GetMapping("/fetch-by-accounting-entity/{accountingEntityId}")
  public ResponseEntity<SuccessFailureResponse<FiscalYearResponse>> fetchByAccountingEntityId(
      @Parameter(
              description = "UUID of the accounting entity that owns the fiscal years",
              required = true,
              example = "550e8400-e29b-41d4-a716-446655440000")
          @PathVariable
          UUID accountingEntityId) {
    List<FiscalYearResponse> fiscalYearsList =
        this.getService().findByAccountingEntity(accountingEntityId);
    return SuccessFailureResponseUtility.createSuccessFailureResponse(
        true, "Record(s) found", HttpStatus.OK, fiscalYearsList);
  }
}
