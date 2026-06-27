import { HttpParams } from "@angular/common/http";
import { Service } from "@angular/core";
import { map, Observable } from "rxjs";

import { CrudApi } from "../api/crud-api";
import { SuccessFailureResponse } from "../api/api-response.model";
import {
  CreateJournalEntryRequest,
  JournalEntryResponse,
  UpdateJournalEntryRequest,
} from "../../shared/models/journal-entry.model";

@Service()
export class JournalEntryApiService extends CrudApi<
  JournalEntryResponse,
  CreateJournalEntryRequest,
  UpdateJournalEntryRequest
> {
  protected override readonly baseUrl = "/api/journal-entries";

  findByAccountingEntityAndFiscalYear(
    accountingEntityId: string,
    fiscalYearId: string
  ): Observable<JournalEntryResponse[]> {
    const params = new HttpParams()
      .set("accountingEntityId", accountingEntityId)
      .set("fiscalYearId", fiscalYearId);

    return this.http
      .get<SuccessFailureResponse<JournalEntryResponse>>(this.baseUrl, {
        params,
      })
      .pipe(map((response) => response.items));
  }

  post(id: string): Observable<JournalEntryResponse> {
    return this.http
      .post<SuccessFailureResponse<JournalEntryResponse>>(
        `${this.baseUrl}/${id}/post`,
        {}
      )
      .pipe(map((response) => this.unwrapOne(response)));
  }
}
