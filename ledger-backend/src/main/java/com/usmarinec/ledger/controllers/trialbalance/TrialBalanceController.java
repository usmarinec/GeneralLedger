package com.usmarinec.ledger.controllers.trialbalance;

import com.usmarinec.ledger.dto.trialbalance.TrialBalanceResponse;
import com.usmarinec.ledger.responses.SuccessFailureResponse;
import com.usmarinec.ledger.responses.SuccessFailureResponseUtility;
import com.usmarinec.ledger.services.trialbalance.TrialBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(TrialBalanceController.API_PATH)
@Tag(
    name = "Trial Balances",
    description = "Generate trial balance reports from posted journal entry activity.")
public class TrialBalanceController {
  public static final String API_PATH = "/trial-balances";

  private final TrialBalanceService trialBalanceService;

  public TrialBalanceController(TrialBalanceService trialBalanceService) {
    this.trialBalanceService = trialBalanceService;
  }

  /**
   * Handles requests to generate an unadjusted trial balance.
   *
   * <p>The report is generated for the supplied accounting entity and fiscal year using posted
   * {@code STANDARD} journal entries only.
   *
   * @param accountingEntityId UUID of the accounting entity for which to generate the trial balance
   * @param fiscalYearId UUID of the fiscal year for which to generate the trial balance
   * @return success response containing the generated unadjusted trial balance
   */
  @Operation(
      summary = "Generate an unadjusted trial balance",
      description =
          "Generates an unadjusted trial balance using posted STANDARD journal entries only.")
  @GetMapping("/unadjusted")
  public ResponseEntity<SuccessFailureResponse<TrialBalanceResponse>>
      generateUnadjustedTrialBalance(
          @Parameter(description = "UUID of the accounting entity", required = true) @RequestParam
              UUID accountingEntityId,
          @Parameter(description = "UUID of the fiscal year", required = true) @RequestParam
              UUID fiscalYearId) {
    TrialBalanceResponse response =
        trialBalanceService.generateUnadjustedTrialBalance(accountingEntityId, fiscalYearId);

    return SuccessFailureResponseUtility.createSuccessFailureResponse(
        true, "Unadjusted trial balance generated", HttpStatus.OK, response);
  }
}
