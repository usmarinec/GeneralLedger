package com.usmarinec.ledger.dto.openapi;

import com.usmarinec.ledger.dto.account.AccountResponse;
import com.usmarinec.ledger.dto.entities.AccountingEntityResponse;
import com.usmarinec.ledger.dto.fiscal.FiscalYearResponse;
import com.usmarinec.ledger.dto.journal.entry.JournalEntryResponse;
import com.usmarinec.ledger.dto.trialbalance.TrialBalanceResponse;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public final class OpenApiResponses {

  private OpenApiResponses() {}

  @Schema(name = "AccountSuccessFailureResponse")
  public record AccountSuccessFailureResponse(
      @Schema(description = "Whether the request completed successfully.", example = "true")
          boolean success,
      @Schema(description = "Human-readable response message.", example = "Record found")
          String message,
      @ArraySchema(schema = @Schema(implementation = AccountResponse.class))
          List<AccountResponse> items,
      @Schema(description = "HTTP status reason phrase.", example = "OK") String httpStatus) {}

  @Schema(name = "AccountingEntitySuccessFailureResponse")
  public record AccountingEntitySuccessFailureResponse(
      @Schema(description = "Whether the request completed successfully.", example = "true")
          boolean success,
      @Schema(description = "Human-readable response message.", example = "Record found")
          String message,
      @ArraySchema(schema = @Schema(implementation = AccountingEntityResponse.class))
          List<AccountingEntityResponse> items,
      @Schema(description = "HTTP status reason phrase.", example = "OK") String httpStatus) {}

  @Schema(name = "FiscalYearSuccessFailureResponse")
  public record FiscalYearSuccessFailureResponse(
      @Schema(description = "Whether the request completed successfully.", example = "true")
          boolean success,
      @Schema(description = "Human-readable response message.", example = "Record found")
          String message,
      @ArraySchema(schema = @Schema(implementation = FiscalYearResponse.class))
          List<FiscalYearResponse> items,
      @Schema(description = "HTTP status reason phrase.", example = "OK") String httpStatus) {}

  @Schema(name = "JournalEntrySuccessFailureResponse")
  public record JournalEntrySuccessFailureResponse(
      @Schema(description = "Whether the request completed successfully.", example = "true")
          boolean success,
      @Schema(description = "Human-readable response message.", example = "Record found")
          String message,
      @ArraySchema(schema = @Schema(implementation = JournalEntryResponse.class))
          List<JournalEntryResponse> items,
      @Schema(description = "HTTP status reason phrase.", example = "OK") String httpStatus) {}

  @Schema(name = "TrialBalanceSuccessFailureResponse")
  public record TrialBalanceSuccessFailureResponse(
      @Schema(description = "Whether the request completed successfully.", example = "true")
          boolean success,
      @Schema(
              description = "Human-readable response message.",
              example = "Unadjusted trial balance generated")
          String message,
      @ArraySchema(schema = @Schema(implementation = TrialBalanceResponse.class))
          List<TrialBalanceResponse> items,
      @Schema(description = "HTTP status reason phrase.", example = "OK") String httpStatus) {}

  @Schema(name = "MessageSuccessFailureResponse")
  public record MessageSuccessFailureResponse(
      @Schema(description = "Whether the request completed successfully.", example = "true")
          boolean success,
      @Schema(description = "Human-readable response message.", example = "Record deleted")
          String message,
      @Schema(description = "HTTP status reason phrase.", example = "OK") String httpStatus) {}
}
