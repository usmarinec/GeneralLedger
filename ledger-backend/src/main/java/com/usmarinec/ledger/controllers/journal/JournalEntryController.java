package com.usmarinec.ledger.controllers.journal;

import com.usmarinec.ledger.controllers.LedgerController;
import com.usmarinec.ledger.domain.journal.JournalEntry;
import com.usmarinec.ledger.dto.journal.entry.CreateJournalEntryRequest;
import com.usmarinec.ledger.dto.journal.entry.JournalEntryResponse;
import com.usmarinec.ledger.dto.journal.entry.UpdateJournalEntryRequest;
import com.usmarinec.ledger.repositories.journal.JournalEntryRepository;
import com.usmarinec.ledger.responses.SuccessFailureResponse;
import com.usmarinec.ledger.responses.SuccessFailureResponseUtility;
import com.usmarinec.ledger.services.journal.JournalEntryService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/journal-entries")
public class JournalEntryController
    extends LedgerController<
        JournalEntry,
        JournalEntryRepository,
        CreateJournalEntryRequest,
        UpdateJournalEntryRequest,
        JournalEntryResponse,
        JournalEntryService> {
  /**
   * Returns all Journal entries for a given AccountingEntity and FiscalYear.
   *
   * @param accountingEntityId UUID for given AccountingEntity
   * @param fiscalYearId UUID for given FiscalYear
   * @return SuccessFailureResponse with applicable JournalEntryResponses
   */
  @GetMapping
  public ResponseEntity<SuccessFailureResponse<JournalEntryResponse>>
      findByAccountingEntityAndFiscalYear(
          @RequestParam UUID accountingEntityId, @RequestParam UUID fiscalYearId) {
    List<JournalEntryResponse> entries =
        this.getService().findByAccountingEntityAndFiscalYear(accountingEntityId, fiscalYearId);
    return SuccessFailureResponseUtility.createSuccessFailureResponse(
        true, "JournalEntries found", HttpStatus.OK, entries);
  }

  /**
   * Posts a given JournalEntry and updates its status.
   *
   * @param id UUID for given JournalEntry
   * @return SuccessFailureResponse with posted JournalEntry
   */
  @PostMapping("/{id}/post")
  public ResponseEntity<SuccessFailureResponse<JournalEntryResponse>> post(@PathVariable UUID id) {
    JournalEntryResponse postResponse = this.getService().post(id);
    return SuccessFailureResponseUtility.createSuccessFailureResponse(
        true, "JournalEntry posted", HttpStatus.OK, postResponse);
  }
}
