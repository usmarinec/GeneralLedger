export type JournalEntryType = 'STANDARD' | 'ADJUSTING' | 'CLOSING';
export type JournalEntryStatus = 'DRAFT' | 'POSTED' | 'VOIDED';

export interface JournalEntryLineResponse {
  id: string;
  lineNumber: number;
  accountId: string;
  accountCode: string;
  accountName: string;
  description: string | null;
  debitAmount: number;
  creditAmount: number;
}

export interface JournalEntryResponse {
  id: string;
  accountingEntityId: string;
  fiscalYearId: string;
  entryDate: string;
  entryType: JournalEntryType;
  status: JournalEntryStatus;
  memo: string | null;
  createdAt: string;
  postedAt: string | null;
  lines: JournalEntryLineResponse[];
}

export interface CreateJournalEntryLineRequest {
  accountId: string;
  description: string | null;
  debitAmount: number;
  creditAmount: number;
}

export interface UpdateJournalEntryLineRequest {
  accountId: string;
  description: string | null;
  debitAmount: number;
  creditAmount: number;
}

export interface CreateJournalEntryRequest {
  accountingEntityId: string;
  fiscalYearId: string;
  entryDate: string;
  entryType: JournalEntryType;
  memo: string | null;
  lines: CreateJournalEntryLineRequest[];
}

export interface UpdateJournalEntryRequest {
  fiscalYearId: string;
  entryDate: string;
  entryType: JournalEntryType;
  memo: string | null;
  lines: UpdateJournalEntryLineRequest[];
}