import { TestBed } from "@angular/core/testing";

import { TrialBalanceApiService } from "./trial-balance-api-service";

describe("TrialBalanceApiService", () => {
  let service: TrialBalanceApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TrialBalanceApiService);
  });

  it("should be created", () => {
    expect(service).toBeTruthy();
  });
});
