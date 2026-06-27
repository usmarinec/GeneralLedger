import { Service } from '@angular/core';

import { CrudApi } from '../api/crud-api';
import {
  AccountingEntityResponse,
  CreateAccountingEntityRequest,
  UpdateAccountingEntityRequest
} from '../../shared/models/accounting-entity.model';

@Service()
export class AccountingEntityApiService extends CrudApi<
  AccountingEntityResponse,
  CreateAccountingEntityRequest,
  UpdateAccountingEntityRequest
> {
  protected override readonly baseUrl = '/api/accounting-entities';
}