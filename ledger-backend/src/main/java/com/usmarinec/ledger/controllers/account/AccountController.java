package com.usmarinec.ledger.controllers.account;

import com.usmarinec.ledger.controllers.LedgerController;
import com.usmarinec.ledger.domain.account.Account;
import com.usmarinec.ledger.dto.account.AccountResponse;
import com.usmarinec.ledger.dto.account.CreateAccountRequest;
import com.usmarinec.ledger.dto.account.UpdateAccountRequest;
import com.usmarinec.ledger.repositories.account.AccountRepository;
import com.usmarinec.ledger.responses.SuccessFailureResponse;
import com.usmarinec.ledger.responses.SuccessFailureResponseUtility;
import com.usmarinec.ledger.services.account.AccountService;
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
@RequestMapping("/account")
@Tag(name = "Accounts", description = "Manage chart-of-account records for an accounting entity.")
public class AccountController
    extends LedgerController<
        Account,
        AccountRepository,
        CreateAccountRequest,
        UpdateAccountRequest,
        AccountResponse,
        AccountService> {
  /**
   * Fetches all accounts by AccountingEntity ID.
   *
   * @param accountingEntityId id to search
   * @return SuccessFailureResponse with List of Accounts
   */
  @Operation(
      summary = "Fetch accounts by accounting entity",
      description =
          "Returns all chart-of-account records belonging to the specified accounting entity.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Accounts found"),
    @ApiResponse(
        responseCode = "404",
        description = "Accounting entity not found",
        content = @Content)
  })
  @GetMapping("/fetch-by-accounting-entity/{accountingEntityId}")
  public ResponseEntity<SuccessFailureResponse<AccountResponse>> fetchByAccountEntityId(
      @Parameter(
              description = "UUID of the accounting entity that owns the accounts",
              required = true,
              example = "550e8400-e29b-41d4-a716-446655440000")
          @PathVariable
          UUID accountingEntityId) {
    List<AccountResponse> accountList =
        this.getService().findByAccountingEntity(accountingEntityId);
    return SuccessFailureResponseUtility.createSuccessFailureResponse(
        true, "Account(s) found", HttpStatus.OK, accountList);
  }
}
