import { AccountType, NormalBalance } from './account.model';

export interface TrialBalanceLineResponse {
  accountId: string;
  accountCode: string;
  accountName: string;
  accountType: AccountType;
  normalBalance: NormalBalance;
  totalDebits: number;
  totalCredits: number;
  debitBalance: number;
  creditBalance: number;
}

export interface TrialBalanceResponse {
  accountingEntityId: string;
  fiscalYearId: string;
  trialBalanceType: string;
  lines: TrialBalanceLineResponse[];
  totalDebitBalance: number;
  totalCreditBalance: number;
  balanced: boolean;
}