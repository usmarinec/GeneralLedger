package com.usmarinec.ledger.controllers.fiscal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.usmarinec.ledger.dto.fiscal.FiscalYearResponse;
import com.usmarinec.ledger.responses.SuccessFailureResponse;
import com.usmarinec.ledger.services.fiscal.FiscalYearService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

class FiscalYearControllerTest {

  private FiscalYearService fiscalYearService;
  private FiscalYearController controller;

  @BeforeEach
  void setUp() {
    fiscalYearService = mock(FiscalYearService.class);
    controller = new FiscalYearController();

    /*
     * Spring is not running in this unit test, so @Autowired in LedgerController
     * is not populated automatically.
     */
    ReflectionTestUtils.setField(controller, "service", fiscalYearService);
  }

  @Test
  void findByAccountingEntity_returnsOkResponseWithFiscalYears() {
    UUID accountingEntityId = UUID.randomUUID();

    List<FiscalYearResponse> fiscalYears =
        List.of(
            new FiscalYearResponse(
                UUID.randomUUID(),
                accountingEntityId,
                2026,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                false),
            new FiscalYearResponse(
                UUID.randomUUID(),
                accountingEntityId,
                2025,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                true));

    when(fiscalYearService.findByAccountingEntity(accountingEntityId)).thenReturn(fiscalYears);

    ResponseEntity<SuccessFailureResponse<FiscalYearResponse>> responseEntity =
        controller.fetchByAccountingEntityId(accountingEntityId);

    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

    SuccessFailureResponse<FiscalYearResponse> body = responseEntity.getBody();

    assertNotNull(body);
    assertEquals(true, body.getSuccess());
    assertEquals("Record(s) found", body.getMessage());
    assertEquals("OK", body.getHttpStatus());
    assertEquals(fiscalYears, body.getItems());

    verify(fiscalYearService).findByAccountingEntity(accountingEntityId);
  }
}