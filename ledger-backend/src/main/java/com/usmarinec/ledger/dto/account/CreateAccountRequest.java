package com.usmarinec.ledger.dto.account;

import com.usmarinec.ledger.domain.account.AccountClassification;
import com.usmarinec.ledger.domain.account.AccountType;
import com.usmarinec.ledger.domain.account.NormalBalance;
import com.usmarinec.ledger.dto.CreateRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(
    name = "CreateAccountRequest",
    description = "Request body for creating a chart-of-accounts record.")
public record CreateAccountRequest(
    @Schema(
            description = "UUID of the accounting entity that owns this account.",
            example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull
        UUID accountingEntityId,
    @Schema(
            description = "Account code used in the chart of accounts.",
            example = "1000",
            maxLength = 50)
        @NotBlank
        @Size(max = 50)
        String code,
    @Schema(description = "Account display name.", example = "Cash", maxLength = 255)
        @NotBlank
        @Size(max = 255)
        String name,
    @Schema(
            description = "High-level account type.",
            example = "ASSET",
            allowableValues = {"ASSET", "LIABILITY", "EQUITY", "REVENUE", "EXPENSE"})
        @NotNull
        AccountType accountType,
    @Schema(
            description = "Normal debit or credit balance for this account.",
            example = "DEBIT",
            allowableValues = {"DEBIT", "CREDIT"})
        @NotNull
        NormalBalance normalBalance,
    @Schema(
            description = "Balance-sheet classification when applicable.",
            example = "CURRENT",
            allowableValues = {"CURRENT", "NON_CURRENT", "NONE"})
        @NotNull
        AccountClassification classification)
    implements CreateRequest {}
