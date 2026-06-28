import { Component, inject, signal } from "@angular/core";
import { CommonModule } from "@angular/common";

import { AccountingEntityApiService } from "../../../core/services/accounting-entity-api-service";
import { FiscalYearApiService } from "../../../core/services/fiscal-year-api-service";
import { LedgerContextStore } from "../../../core/state/ledger-context.store";

import { AccountingEntityResponse } from "../../models/accounting-entity.model";
import { FiscalYearResponse } from "../../models/fiscal-year.model";

@Component({
  selector: "app-ledger-context-selector",
  imports: [CommonModule],
  templateUrl: "./ledger-context-selector.html",
  styleUrl: "./ledger-context-selector.scss",
})
export class LedgerContextSelector {
  private readonly accountingEntityApi = inject(AccountingEntityApiService);
  private readonly fiscalYearApi = inject(FiscalYearApiService);

  readonly context = inject(LedgerContextStore);

  readonly accountingEntities = signal<AccountingEntityResponse[]>([]);
  readonly fiscalYears = signal<FiscalYearResponse[]>([]);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.loadAccountingEntities();

    const selectedAccountingEntityId =
      this.context.selectedAccountingEntityId();

    if (selectedAccountingEntityId) {
      this.loadFiscalYears(selectedAccountingEntityId);
    }
  }

  onAccountingEntityChange(accountingEntityId: string): void {
    if (!accountingEntityId) {
      this.context.clearAccountingEntity();
      this.fiscalYears.set([]);
      return;
    }

    this.context.selectAccountingEntity(accountingEntityId);
    this.loadFiscalYears(accountingEntityId);
  }

  onFiscalYearChange(fiscalYearId: string): void {
    if (!fiscalYearId) {
      this.context.clearFiscalYear();
      return;
    }

    this.context.selectFiscalYear(fiscalYearId);
  }

  private loadAccountingEntities(): void {
    this.accountingEntityApi.findAll().subscribe({
      next: (entities) => this.accountingEntities.set(entities),
      error: (error) => this.handleError(error),
    });
  }

  private loadFiscalYears(accountingEntityId: string): void {
    this.fiscalYearApi.findByAccountingEntity(accountingEntityId).subscribe({
      next: (fiscalYears) => this.fiscalYears.set(fiscalYears),
      error: (error) => this.handleError(error),
    });
  }

  private handleError(error: any): void {
    const message =
      error?.error?.message ??
      error?.error?.items?.[0]?.message ??
      error?.message ??
      "Unexpected error";

    this.error.set(message);
  }
}
