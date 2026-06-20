package com.usmarinec.ledger.dto.account;

import com.usmarinec.ledger.domain.account.AccountClassification;
import com.usmarinec.ledger.domain.account.AccountType;
import com.usmarinec.ledger.domain.account.NormalBalance;
import com.usmarinec.ledger.dto.Response;
import java.util.UUID;

public record AccountResponse(
    UUID id,
    UUID accountingEntityId,
    String code,
    String name,
    AccountType accountType,
    NormalBalance normalBalance,
    AccountClassification classification,
    boolean active)
    implements Response {}
