import { TestBed } from "@angular/core/testing";

import { LedgerContextStore } from "./ledger-context.store";

describe("LedgerContextStore", () => {
  let service: LedgerContextStore;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LedgerContextStore);
  });

  it("should be created", () => {
    expect(service).toBeTruthy();
  });
});
