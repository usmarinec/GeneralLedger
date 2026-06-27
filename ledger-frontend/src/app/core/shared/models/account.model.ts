export type AccountType = 'ASSET' | 'LIABILITY' | 'EQUITY' | 'REVENUE' | 'EXPENSE';
export type NormalBalance = 'DEBIT' | 'CREDIT';
export type AccountClassification = 'CURRENT' | 'NON_CURRENT' | 'NONE';

export interface AccountResponse {
  id: string;
  accountingEntityId: string;
  code: string;
  name: string;
  accountType: AccountType;
  normalBalance: NormalBalance;
  classification: AccountClassification;
  active: boolean;
}

export interface CreateAccountRequest {
  accountingEntityId: string;
  code: string;
  name: string;
  accountType: AccountType;
  normalBalance: NormalBalance;
  classification: AccountClassification;
}

export interface UpdateAccountRequest extends CreateAccountRequest {
  active: boolean;
}