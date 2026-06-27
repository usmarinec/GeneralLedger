// src/app/core/services/fiscal-year-api.ts

import { Service } from '@angular/core';
import { map, Observable } from 'rxjs';

import { CrudApi } from '../api/crud-api';
import { SuccessFailureResponse } from '../api/api-response.model';
import {
  CreateFiscalYearRequest,
  FiscalYearResponse,
  UpdateFiscalYearRequest
} from '../../shared/models/fiscal-year.model';

@Service()
export class FiscalYearApiService extends CrudApi<
  FiscalYearResponse,
  CreateFiscalYearRequest,
  UpdateFiscalYearRequest
> {
  protected override readonly baseUrl = '/api/fiscal-years';

  findByAccountingEntity(accountingEntityId: string): Observable<FiscalYearResponse[]> {
    return this.http
      .get<SuccessFailureResponse<FiscalYearResponse>>(
        `${this.baseUrl}/fetch-by-accounting-entity/${accountingEntityId}`
      )
      .pipe(map((response) => response.items));
  }
}