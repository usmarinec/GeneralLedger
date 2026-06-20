package com.usmarinec.ledger.controllers.journal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.usmarinec.ledger.domain.journal.JournalEntryStatus;
import com.usmarinec.ledger.domain.journal.JournalEntryType;
import com.usmarinec.ledger.dto.journal.entry.JournalEntryResponse;
import com.usmarinec.ledger.dto.journal.line.JournalEntryLineResponse;
import com.usmarinec.ledger.responses.SuccessFailureResponse;
import com.usmarinec.ledger.services.journal.JournalEntryService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

class JournalEntryControllerTest {

  private JournalEntryService journalEntryService;
  private JournalEntryController controller;

  @BeforeEach
  void setUp() {
    journalEntryService = mock(JournalEntryService.class);
    controller = new JournalEntryController();

    /*
     * Spring is not running in this unit test, so @Autowired in LedgerController
     * is not populated automatically.
     */
    ReflectionTestUtils.setField(controller, "service", journalEntryService);
  }

  @Test
  void findByAccountingEntityAndFiscalYear_returnsOkResponseWithJournalEntries() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID fiscalYearId = UUID.randomUUID();

    List<JournalEntryResponse> journalEntries =
        List.of(
            journalEntryResponse(
                UUID.randomUUID(), accountingEntityId, fiscalYearId, JournalEntryStatus.DRAFT),
            journalEntryResponse(
                UUID.randomUUID(), accountingEntityId, fiscalYearId, JournalEntryStatus.POSTED));

    when(journalEntryService.findByAccountingEntityAndFiscalYear(accountingEntityId, fiscalYearId))
        .thenReturn(journalEntries);

    ResponseEntity<SuccessFailureResponse<JournalEntryResponse>> responseEntity =
        controller.findByAccountingEntityAndFiscalYear(accountingEntityId, fiscalYearId);

    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

    SuccessFailureResponse<JournalEntryResponse> body = responseEntity.getBody();

    assertNotNull(body);
    assertEquals(true, body.getSuccess());
    assertEquals("JournalEntries found", body.getMessage());
    assertEquals("OK", body.getHttpStatus());
    assertEquals(journalEntries, body.getItems());

    verify(journalEntryService)
        .findByAccountingEntityAndFiscalYear(accountingEntityId, fiscalYearId);
  }

  @Test
  void post_postsJournalEntryAndReturnsOkResponse() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID fiscalYearId = UUID.randomUUID();
    UUID journalEntryId = UUID.randomUUID();

    JournalEntryResponse postedResponse =
        journalEntryResponse(
            journalEntryId, accountingEntityId, fiscalYearId, JournalEntryStatus.POSTED);

    when(journalEntryService.post(journalEntryId)).thenReturn(postedResponse);

    ResponseEntity<SuccessFailureResponse<JournalEntryResponse>> responseEntity =
        controller.post(journalEntryId);

    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

    SuccessFailureResponse<JournalEntryResponse> body = responseEntity.getBody();

    assertNotNull(body);
    assertEquals(true, body.getSuccess());
    assertEquals("JournalEntry posted", body.getMessage());
    assertEquals("OK", body.getHttpStatus());
    assertEquals(1, body.getItems().size());
    assertEquals(postedResponse, body.getItems().get(0));

    verify(journalEntryService).post(journalEntryId);
  }

  private JournalEntryResponse journalEntryResponse(
      UUID journalEntryId, UUID accountingEntityId, UUID fiscalYearId, JournalEntryStatus status) {
    UUID cashAccountId = UUID.randomUUID();
    UUID equityAccountId = UUID.randomUUID();

    return new JournalEntryResponse(
        journalEntryId,
        accountingEntityId,
        fiscalYearId,
        LocalDate.of(2026, 1, 1),
        JournalEntryType.STANDARD,
        status,
        "Test journal entry",
        LocalDateTime.of(2026, 1, 1, 12, 0),
        status == JournalEntryStatus.POSTED ? LocalDateTime.of(2026, 1, 1, 13, 0) : null,
        List.of(
            new JournalEntryLineResponse(
                UUID.randomUUID(),
                1,
                cashAccountId,
                "1000",
                "Cash",
                "Cash received",
                new BigDecimal("100.00"),
                BigDecimal.ZERO),
            new JournalEntryLineResponse(
                UUID.randomUUID(),
                2,
                equityAccountId,
                "3000",
                "Owner's Equity",
                "Owner contribution",
                BigDecimal.ZERO,
                new BigDecimal("100.00"))));
  }
}
