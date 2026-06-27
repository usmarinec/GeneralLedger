export interface AccountingEntityResponse {
  id: string;
  name: string;
  createdAt: string;
}

export interface CreateAccountingEntityRequest {
  name: string;
}

export interface UpdateAccountingEntityRequest {
  name: string;
}
