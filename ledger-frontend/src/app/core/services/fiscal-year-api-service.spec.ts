import { TestBed } from "@angular/core/testing";

import { FiscalYearApiService } from "./fiscal-year-api-service";

describe("FiscalYearApiService", () => {
  let service: FiscalYearApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FiscalYearApiService);
  });

  it("should be created", () => {
    expect(service).toBeTruthy();
  });
});
