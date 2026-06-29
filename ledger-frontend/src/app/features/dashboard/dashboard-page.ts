import { CommonModule } from "@angular/common";
import { Component, computed, effect, inject, signal } from "@angular/core";
import { RouterLink } from "@angular/router";

import { AccountingEntityApiService } from "../../core/services/accounting-entity-api-service";
import { AccountApiService } from "../../core/services/account-api-service";
import { FiscalYearApiService } from "../../core/services/fiscal-year-api-service";
import { JournalEntryApiService } from "../../core/services/journal-entry-api-service";
import { TrialBalanceApiService } from "../../core/services/trial-balance-api-service";
import { LedgerContextStore } from "../../core/state/ledger-context.store";
import { LedgerContextSelector } from "../../shared/components/ledger-context-selector/ledger-context-selector";

import { AccountResponse } from "../../shared/models/account.model";
import { AccountingEntityResponse } from "../../shared/models/accounting-entity.model";
import { FiscalYearResponse } from "../../shared/models/fiscal-year.model";
import { JournalEntryResponse } from "../../shared/models/journal-entry.model";
import { TrialBalanceResponse } from "../../shared/models/trial-balance.model";

@Component({
  selector: "app-dashboard-page",
  imports: [CommonModule, RouterLink, LedgerContextSelector],
  templateUrl: "./dashboard-page.html",
  styleUrl: "./dashboard-page.scss",
})
export class DashboardPage {
  private readonly accountingEntityApi = inject(AccountingEntityApiService);
  private readonly fiscalYearApi = inject(FiscalYearApiService);
  private readonly accountApi = inject(AccountApiService);
  private readonly journalEntryApi = inject(JournalEntryApiService);
  private readonly trialBalanceApi = inject(TrialBalanceApiService);

  readonly context = inject(LedgerContextStore);

  readonly accountingEntities = signal<AccountingEntityResponse[]>([]);
  readonly fiscalYears = signal<FiscalYearResponse[]>([]);
  readonly accounts = signal<AccountResponse[]>([]);
  readonly journalEntries = signal<JournalEntryResponse[]>([]);
  readonly trialBalance = signal<TrialBalanceResponse | null>(null);

  readonly loading = signal(false);
  readonly message = signal<string | null>(null);
  readonly error = signal<string | null>(null);

  readonly selectedAccountingEntity = computed(() => {
    const selectedId = this.context.selectedAccountingEntityId();

    return (
      this.accountingEntities().find((entity) => entity.id === selectedId) ??
      null
    );
  });

  readonly selectedFiscalYear = computed(() => {
    const selectedId = this.context.selectedFiscalYearId();

    return (
      this.fiscalYears().find((fiscalYear) => fiscalYear.id === selectedId) ??
      null
    );
  });

  readonly draftJournalEntryCount = computed(
    () =>
      this.journalEntries().filter((entry) => entry.status === "DRAFT").length
  );

  readonly postedJournalEntryCount = computed(
    () =>
      this.journalEntries().filter((entry) => entry.status === "POSTED").length
  );

  readonly voidedJournalEntryCount = computed(
    () =>
      this.journalEntries().filter((entry) => entry.status === "VOIDED").length
  );

  readonly activeAccountCount = computed(
    () => this.accounts().filter((account) => account.active).length
  );

  readonly inactiveAccountCount = computed(
    () => this.accounts().filter((account) => !account.active).length
  );

  readonly hasMinimumJournalEntrySetup = computed(
    () => this.context.hasFullContext() && this.activeAccountCount() >= 2
  );

  constructor() {
    effect(() => {
      const accountingEntityId = this.context.selectedAccountingEntityId();
      const fiscalYearId = this.context.selectedFiscalYearId();

      this.trialBalance.set(null);
      this.message.set(null);
      this.error.set(null);

      this.loadAccountingEntities();

      if (accountingEntityId) {
        this.loadFiscalYears(accountingEntityId);
        this.loadAccounts(accountingEntityId);
      } else {
        this.fiscalYears.set([]);
        this.accounts.set([]);
      }

      if (accountingEntityId && fiscalYearId) {
        this.loadJournalEntries(accountingEntityId, fiscalYearId);
      } else {
        this.journalEntries.set([]);
      }
    });
  }

  refresh(): void {
    const accountingEntityId = this.context.selectedAccountingEntityId();
    const fiscalYearId = this.context.selectedFiscalYearId();

    this.clearAlerts();
    this.loadAccountingEntities();

    if (accountingEntityId) {
      this.loadFiscalYears(accountingEntityId);
      this.loadAccounts(accountingEntityId);
    }

    if (accountingEntityId && fiscalYearId) {
      this.loadJournalEntries(accountingEntityId, fiscalYearId);
    }
  }

  generateTrialBalancePreview(): void {
    const accountingEntityId = this.context.selectedAccountingEntityId();
    const fiscalYearId = this.context.selectedFiscalYearId();

    if (!accountingEntityId || !fiscalYearId) {
      this.error.set("Select an accounting entity and fiscal year first.");
      return;
    }

    this.clearAlerts();
    this.loading.set(true);

    this.trialBalanceApi
      .generateUnadjusted(accountingEntityId, fiscalYearId)
      .subscribe({
        next: (trialBalance) => {
          this.trialBalance.set(trialBalance);
          this.message.set("Trial balance preview generated.");
          this.loading.set(false);
        },
        error: (error) => {
          this.loading.set(false);
          this.handleError(error);
        },
      });
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

  private loadAccounts(accountingEntityId: string): void {
    this.accountApi.findByAccountingEntity(accountingEntityId).subscribe({
      next: (accounts) => this.accounts.set(accounts),
      error: (error) => this.handleError(error),
    });
  }

  private loadJournalEntries(
    accountingEntityId: string,
    fiscalYearId: string
  ): void {
    this.journalEntryApi
      .findByAccountingEntityAndFiscalYear(accountingEntityId, fiscalYearId)
      .subscribe({
        next: (journalEntries) => this.journalEntries.set(journalEntries),
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
