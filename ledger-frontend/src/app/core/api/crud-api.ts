import { HttpClient } from '@angular/common/http';
import { inject } from '@angular/core';
import { map, Observable } from 'rxjs';

import { SuccessFailureResponse } from './api-response.model';

export abstract class CrudApi<ResponseT, CreateRequestT, UpdateRequestT> {
  protected readonly http = inject(HttpClient);

  protected abstract readonly baseUrl: string;

  create(request: CreateRequestT): Observable<ResponseT> {
    return this.http
      .post<SuccessFailureResponse<ResponseT>>(`${this.baseUrl}/create`, request)
      .pipe(map((response) => this.unwrapOne(response)));
  }

  createList(requests: CreateRequestT[]): Observable<ResponseT[]> {
    return this.http
      .post<SuccessFailureResponse<ResponseT>>(`${this.baseUrl}/create-list`, requests)
      .pipe(map((response) => response.items));
  }

  findById(id: string): Observable<ResponseT> {
    return this.http
      .get<SuccessFailureResponse<ResponseT>>(`${this.baseUrl}/fetch/${id}`)
      .pipe(map((response) => this.unwrapOne(response)));
  }

  findAll(): Observable<ResponseT[]> {
    return this.http
      .get<SuccessFailureResponse<ResponseT>>(`${this.baseUrl}/fetch`)
      .pipe(map((response) => response.items));
  }

  update(id: string, request: UpdateRequestT): Observable<ResponseT> {
    return this.http
      .put<SuccessFailureResponse<ResponseT>>(`${this.baseUrl}/update/${id}`, request)
      .pipe(map((response) => this.unwrapOne(response)));
  }

  delete(id: string): Observable<void> {
    return this.http
      .delete<SuccessFailureResponse<ResponseT>>(`${this.baseUrl}/delete/${id}`)
      .pipe(map(() => undefined));
  }

  protected unwrapOne(response: SuccessFailureResponse<ResponseT>): ResponseT {
    if (!response.items?.length) {
      throw new Error('Expected response to contain one item.');
    }

    return response.items[0];
  }
}