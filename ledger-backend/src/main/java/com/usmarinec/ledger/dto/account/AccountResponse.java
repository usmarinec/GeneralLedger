package com.usmarinec.ledger.dto.account;

import com.usmarinec.ledger.domain.account.AccountClassification;
import com.usmarinec.ledger.domain.account.AccountType;
import com.usmarinec.ledger.domain.account.NormalBalance;
import com.usmarinec.ledger.dto.Response;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(name = "AccountResponse", description = "Chart-of-accounts record returned by the API.")
public record AccountResponse(
    @Schema(
            description = "Unique account identifier.",
            example = "2a2c0af1-28a1-4585-a6d2-92285f297c7c")
        UUID id,
    @Schema(
            description = "Owning accounting entity identifier.",
            example = "550e8400-e29b-41d4-a716-446655440000")
        UUID accountingEntityId,
    @Schema(description = "Account code.", example = "1000") String code,
    @Schema(description = "Account display name.", example = "Cash") String name,
    @Schema(description = "Account type.", example = "ASSET") AccountType accountType,
    @Schema(description = "Normal account balance.", example = "DEBIT") NormalBalance normalBalance,
    @Schema(description = "Account classification.", example = "CURRENT")
        AccountClassification classification,
    @Schema(description = "Whether the account is active.", example = "true") boolean active)
    implements Response {}
