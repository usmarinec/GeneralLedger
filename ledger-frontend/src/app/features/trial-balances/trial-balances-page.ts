import { CommonModule } from "@angular/common";
import { Component, effect, inject, signal } from "@angular/core";

import { TrialBalanceApiService } from "../../core/services/trial-balance-api-service";
import { LedgerContextStore } from "../../core/state/ledger-context.store";
import { LedgerContextSelector } from "../../shared/components/ledger-context-selector/ledger-context-selector";
import { TrialBalanceResponse } from "../../shared/models/trial-balance.model";

@Component({
  selector: "app-trial-balances-page",
  imports: [CommonModule, LedgerContextSelector],
  templateUrl: "./trial-balances-page.html",
  styleUrl: "./trial-balances-page.scss",
})
export class TrialBalancesPage {
  private readonly trialBalanceApi = inject(TrialBalanceApiService);

  readonly context = inject(LedgerContextStore);

  readonly trialBalance = signal<TrialBalanceResponse | null>(null);
  readonly message = signal<string | null>(null);
  readonly error = signal<string | null>(null);

  constructor() {
    effect(() => {
      this.context.selectedAccountingEntityId();
      this.context.selectedFiscalYearId();

      this.trialBalance.set(null);
      this.message.set(null);
      this.error.set(null);
    });
  }

  generateUnadjustedTrialBalance(): void {
    const accountingEntityId = this.context.selectedAccountingEntityId();
    const fiscalYearId = this.context.selectedFiscalYearId();

    if (!accountingEntityId || !fiscalYearId) {
      this.error.set(
        "Select an accounting entity and fiscal year before generating a trial balance."
      );
      return;
    }

    this.clearAlerts();

    this.trialBalanceApi
      .generateUnadjusted(accountingEntityId, fiscalYearId)
      .subscribe({
        next: (trialBalance) => {
          this.trialBalance.set(trialBalance);
          this.message.set("Unadjusted trial balance generated.");
        },
        error: (error) => this.handleError(error),
      });
  }

  private clearAlerts(): void {
    this.message.set(null);
    this.error.set(null);
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
