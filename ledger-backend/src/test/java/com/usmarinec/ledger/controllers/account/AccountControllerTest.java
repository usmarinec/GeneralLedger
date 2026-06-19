package com.usmarinec.ledger.controllers.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.usmarinec.ledger.domain.account.AccountClassification;
import com.usmarinec.ledger.domain.account.AccountType;
import com.usmarinec.ledger.domain.account.NormalBalance;
import com.usmarinec.ledger.dto.account.AccountResponse;
import com.usmarinec.ledger.responses.SuccessFailureResponse;
import com.usmarinec.ledger.services.account.AccountService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

class AccountControllerTest {

  private AccountService accountService;
  private AccountController controller;

  @BeforeEach
  void setUp() {
    accountService = mock(AccountService.class);
    controller = new AccountController();

    /*
     * Spring is not running in this unit test, so @Autowired in LedgerController
     * is not populated automatically.
     */
    ReflectionTestUtils.setField(controller, "service", accountService);
  }

  @Test
  void fetchByAccountEntityId_returnsOkResponseWithAccounts() {
    UUID accountingEntityId = UUID.randomUUID();

    List<AccountResponse> accounts =
        List.of(
            new AccountResponse(
                UUID.randomUUID(),
                accountingEntityId,
                "1000",
                "Cash",
                AccountType.ASSET,
                NormalBalance.DEBIT,
                AccountClassification.CURRENT,
                true),
            new AccountResponse(
                UUID.randomUUID(),
                accountingEntityId,
                "4000",
                "Service Revenue",
                AccountType.REVENUE,
                NormalBalance.CREDIT,
                AccountClassification.NONE,
                true));

    when(accountService.findByAccountingEntity(accountingEntityId)).thenReturn(accounts);

    ResponseEntity<SuccessFailureResponse<AccountResponse>> responseEntity =
        controller.fetchByAccountEntityId(accountingEntityId);

    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

    SuccessFailureResponse<AccountResponse> body = responseEntity.getBody();

    assertNotNull(body);
    assertEquals(true, body.getSuccess());
    assertEquals("Account(s) found", body.getMessage());
    assertEquals("OK", body.getHttpStatus());
    assertEquals(accounts, body.getItems());

    verify(accountService).findByAccountingEntity(accountingEntityId);
  }
}
