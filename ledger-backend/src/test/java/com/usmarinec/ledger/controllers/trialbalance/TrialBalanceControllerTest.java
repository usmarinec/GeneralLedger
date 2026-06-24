package com.usmarinec.ledger.controllers.trialbalance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.usmarinec.ledger.domain.account.AccountType;
import com.usmarinec.ledger.domain.account.NormalBalance;
import com.usmarinec.ledger.dto.trialbalance.TrialBalanceLineResponse;
import com.usmarinec.ledger.dto.trialbalance.TrialBalanceResponse;
import com.usmarinec.ledger.responses.SuccessFailureResponse;
import com.usmarinec.ledger.services.trialbalance.TrialBalanceService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class TrialBalanceControllerTest {

  private TrialBalanceService trialBalanceService;
  private TrialBalanceController controller;

  @BeforeEach
  void setUp() {
    trialBalanceService = mock(TrialBalanceService.class);
    controller = new TrialBalanceController(trialBalanceService);
  }

  @Test
  void generateUnadjustedTrialBalance_returnsOkResponseWithTrialBalance() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID fiscalYearId = UUID.randomUUID();

    TrialBalanceResponse trialBalanceResponse =
        new TrialBalanceResponse(
            accountingEntityId,
            fiscalYearId,
            "UNADJUSTED",
            List.of(
                new TrialBalanceLineResponse(
                    UUID.randomUUID(),
                    "1000",
                    "Cash",
                    AccountType.ASSET,
                    NormalBalance.DEBIT,
                    new BigDecimal("100.00"),
                    BigDecimal.ZERO,
                    new BigDecimal("100.00"),
                    BigDecimal.ZERO),
                new TrialBalanceLineResponse(
                    UUID.randomUUID(),
                    "4000",
                    "Service Revenue",
                    AccountType.REVENUE,
                    NormalBalance.CREDIT,
                    BigDecimal.ZERO,
                    new BigDecimal("100.00"),
                    BigDecimal.ZERO,
                    new BigDecimal("100.00"))),
            new BigDecimal("100.00"),
            new BigDecimal("100.00"),
            true);

    when(trialBalanceService.generateUnadjustedTrialBalance(accountingEntityId, fiscalYearId))
        .thenReturn(trialBalanceResponse);

    ResponseEntity<SuccessFailureResponse<TrialBalanceResponse>> responseEntity =
        controller.generateUnadjustedTrialBalance(accountingEntityId, fiscalYearId);

    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

    SuccessFailureResponse<TrialBalanceResponse> body = responseEntity.getBody();

    assertNotNull(body);
    assertEquals(true, body.getSuccess());
    assertEquals("Unadjusted trial balance generated", body.getMessage());
    assertEquals("OK", body.getHttpStatus());
    assertEquals(1, body.getItems().size());
    assertEquals(trialBalanceResponse, body.getItems().get(0));

    verify(trialBalanceService).generateUnadjustedTrialBalance(accountingEntityId, fiscalYearId);
  }
}
