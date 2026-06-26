package com.usmarinec.ledger.dto.trialbalance;

import com.usmarinec.ledger.dto.Response;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TrialBalanceResponse(
    UUID accountingEntityId,
    UUID fiscalYearId,
    String trialBalanceType,
    List<TrialBalanceLineResponse> lines,
    BigDecimal totalDebitBalance,
    BigDecimal totalCreditBalance,
    boolean balanced)
    implements Response {}
