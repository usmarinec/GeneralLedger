package com.usmarinec.ledger.dto.trialbalance;

import com.usmarinec.ledger.domain.account.AccountType;
import com.usmarinec.ledger.domain.account.NormalBalance;
import java.math.BigDecimal;
import java.util.UUID;

public record TrialBalanceLineResponse(
    UUID accountId,
    String accountCode,
    String accountName,
    AccountType accountType,
    NormalBalance normalBalance,
    BigDecimal totalDebits,
    BigDecimal totalCredits,
    BigDecimal debitBalance,
    BigDecimal creditBalance) {}
