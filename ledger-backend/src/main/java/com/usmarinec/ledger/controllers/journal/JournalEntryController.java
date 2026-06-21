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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/journal-entries")
@Tag(
    name = "Journal Entries",
    description =
        "Create, update, fetch, and post journal entries with balanced debit and credit lines.")
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
  @Operation(
      summary = "Fetch journal entries by accounting entity and fiscal year",
      description =
          "Returns all journal entries for the specified accounting entity and fiscal year.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Journal entries found"),
    @ApiResponse(
        responseCode = "404",
        description = "Accounting entity or fiscal year not found",
        content = @Content)
  })
  @GetMapping
  public ResponseEntity<SuccessFailureResponse<JournalEntryResponse>>
      findByAccountingEntityAndFiscalYear(
          @Parameter(
                  description = "UUID of the accounting entity",
                  required = true,
                  example = "550e8400-e29b-41d4-a716-446655440000")
              @RequestParam
              UUID accountingEntityId,
          @Parameter(
                  description = "UUID of the fiscal year",
                  required = true,
                  example = "9b680f75-7d7e-456b-9439-18fcdba3e6f1")
              @RequestParam
              UUID fiscalYearId) {
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
  @Operation(
      summary = "Post a journal entry",
      description =
          "Posts a draft journal entry. Posting changes the journal entry status and makes it part of ledger activity.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Journal entry posted"),
    @ApiResponse(
        responseCode = "400",
        description = "Journal entry cannot be posted",
        content = @Content),
    @ApiResponse(responseCode = "404", description = "Journal entry not found", content = @Content),
    @ApiResponse(
        responseCode = "409",
        description = "Journal entry is already posted or conflicts with ledger rules",
        content = @Content)
  })
  @PostMapping("/{id}/post")
  public ResponseEntity<SuccessFailureResponse<JournalEntryResponse>> post(
      @Parameter(
              description = "UUID of the journal entry to post",
              required = true,
              example = "e3f9f6c1-0f7d-4f1b-90be-263d2a6fa4fd")
          @PathVariable
          UUID id) {
    JournalEntryResponse postResponse = this.getService().post(id);
    return SuccessFailureResponseUtility.createSuccessFailureResponse(
        true, "JournalEntry posted", HttpStatus.OK, postResponse);
  }
}
