export interface FiscalYearResponse {
  id: string;
  accountingEntityId: string;
  year: number;
  startDate: string;
  endDate: string;
  closed: boolean;
}

export interface CreateFiscalYearRequest {
  accountingEntityId: string;
  year: number;
  startDate: string;
  endDate: string;
}

export interface UpdateFiscalYearRequest {
  accountingEntityId: string;
  year: number;
  startDate: string;
  endDate: string;
  closed: boolean;
}
