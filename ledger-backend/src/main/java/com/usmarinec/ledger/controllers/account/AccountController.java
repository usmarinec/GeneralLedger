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
  @GetMapping("/fetch-by-accounting-entity/{accountingEntityId}")
  public ResponseEntity<SuccessFailureResponse<AccountResponse>> fetchByAccountEntityId(
      @PathVariable UUID accountingEntityId) {
    List<AccountResponse> accountList =
        this.getService().findByAccountingEntity(accountingEntityId);
    return SuccessFailureResponseUtility.createSuccessFailureResponse(
        true, "Account(s) found", HttpStatus.OK, accountList);
  }
}
