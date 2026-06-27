import { Service } from "@angular/core";
import { map, Observable } from "rxjs";

import { CrudApi } from "../api/crud-api";
import { SuccessFailureResponse } from "../api/api-response.model";
import {
  AccountResponse,
  CreateAccountRequest,
  UpdateAccountRequest,
} from "../../shared/models/account.model";

@Service()
export class AccountApiService extends CrudApi<
  AccountResponse,
  CreateAccountRequest,
  UpdateAccountRequest
> {
  protected override readonly baseUrl = "/api/accounts";

  findByAccountingEntity(
    accountingEntityId: string
  ): Observable<AccountResponse[]> {
    return this.http
      .get<SuccessFailureResponse<AccountResponse>>(
        `${this.baseUrl}/fetch-by-accounting-entity/${accountingEntityId}`
      )
      .pipe(map((response) => response.items));
  }
}
