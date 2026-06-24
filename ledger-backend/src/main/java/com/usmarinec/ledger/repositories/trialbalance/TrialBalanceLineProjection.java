package com.usmarinec.ledger.repositories.trialbalance;

import com.usmarinec.ledger.domain.account.AccountType;
import com.usmarinec.ledger.domain.account.NormalBalance;
import java.math.BigDecimal;
import java.util.UUID;

public interface TrialBalanceLineProjection {
  UUID getAccountId();

  String getAccountCode();

  String getAccountName();

  AccountType getAccountType();

  NormalBalance getNormalBalance();

  BigDecimal getTotalDebits();

  BigDecimal getTotalCredits();
}
