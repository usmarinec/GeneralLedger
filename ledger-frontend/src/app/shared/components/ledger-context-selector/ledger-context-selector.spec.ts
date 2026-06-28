import { ComponentFixture, TestBed } from "@angular/core/testing";

import { LedgerContextSelector } from "./ledger-context-selector";

describe("LedgerContextSelector", () => {
  let component: LedgerContextSelector;
  let fixture: ComponentFixture<LedgerContextSelector>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LedgerContextSelector],
    }).compileComponents();

    fixture = TestBed.createComponent(LedgerContextSelector);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
