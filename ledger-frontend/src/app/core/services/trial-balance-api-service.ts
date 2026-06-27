import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { map, Observable } from 'rxjs';

import { SuccessFailureResponse } from '../api/api-response.model';
import { TrialBalanceResponse } from '../../shared/models/trial-balance.model';

@Service()
export class TrialBalanceApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/trial-balances';

  generateUnadjusted(
    accountingEntityId: string,
    fiscalYearId: string
  ): Observable<TrialBalanceResponse> {
    const params = new HttpParams()
      .set('accountingEntityId', accountingEntityId)
      .set('fiscalYearId', fiscalYearId);

    return this.http
      .get<SuccessFailureResponse<TrialBalanceResponse>>(`${this.baseUrl}/unadjusted`, { params })
      .pipe(map((response) => response.items[0]));
  }
}