package com.usmarinec.ledger.dto.account;

import com.usmarinec.ledger.domain.account.AccountClassification;
import com.usmarinec.ledger.domain.account.AccountType;
import com.usmarinec.ledger.domain.account.NormalBalance;
import com.usmarinec.ledger.dto.UpdateRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateAccountRequest(
    @NotNull UUID accountingEntityId,
    @NotBlank @Size(max = 50) String code,
    @NotBlank @Size(max = 255) String name,
    @NotNull AccountType accountType,
    @NotNull NormalBalance normalBalance,
    @NotNull AccountClassification classification,
    boolean active)
    implements UpdateRequest {}
