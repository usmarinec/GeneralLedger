import { TestBed } from '@angular/core/testing';

import { AccountingEntityApiService } from './accounting-entity-api-service';

describe('AccountingEntityApiService', () => {
  let service: AccountingEntityApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AccountingEntityApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
