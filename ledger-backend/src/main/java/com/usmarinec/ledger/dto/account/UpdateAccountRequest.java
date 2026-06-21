package com.usmarinec.ledger.dto.account;

import com.usmarinec.ledger.domain.account.AccountClassification;
import com.usmarinec.ledger.domain.account.AccountType;
import com.usmarinec.ledger.domain.account.NormalBalance;
import com.usmarinec.ledger.dto.UpdateRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(
    name = "UpdateAccountRequest",
    description = "Request body for updating a chart-of-accounts record.")
public record UpdateAccountRequest(
    @Schema(description = "Updated account code.", example = "1000", maxLength = 50)
        @NotBlank
        @Size(max = 50)
        String code,
    @Schema(description = "Updated account display name.", example = "Cash", maxLength = 255)
        @NotBlank
        @Size(max = 255)
        String name,
    @Schema(
            description = "Updated account type.",
            example = "ASSET",
            allowableValues = {"ASSET", "LIABILITY", "EQUITY", "REVENUE", "EXPENSE"})
        @NotNull
        AccountType accountType,
    @Schema(
            description = "Updated normal balance.",
            example = "DEBIT",
            allowableValues = {"DEBIT", "CREDIT"})
        @NotNull
        NormalBalance normalBalance,
    @Schema(
            description = "Updated classification.",
            example = "CURRENT",
            allowableValues = {"CURRENT", "NON_CURRENT", "NONE"})
        @NotNull
        AccountClassification classification,
    @Schema(
            description = "Whether the account is active and available for journal entries.",
            example = "true")
        boolean active)
    implements UpdateRequest {}
