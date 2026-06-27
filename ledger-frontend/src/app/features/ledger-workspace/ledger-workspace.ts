import { Component, computed, inject, signal } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  FormArray,
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";

import { AccountingEntityApiService } from "../../core/services/accounting-entity-api-service";
import { FiscalYearApiService } from "../../core/services/fiscal-year-api-service";
import { AccountApiService } from "../../core/services/account-api-service";
import { JournalEntryApiService } from "../../core/services/journal-entry-api-service";
import { TrialBalanceApiService } from "../../core/services/trial-balance-api-service";

import { AccountingEntityResponse } from "../../shared/models/accounting-entity.model";
import { FiscalYearResponse } from "../../shared/models/fiscal-year.model";
import { AccountResponse } from "../../shared/models/account.model";
import { JournalEntryResponse } from "../../shared/models/journal-entry.model";
import { TrialBalanceResponse } from "../../shared/models/trial-balance.model";

@Component({
  selector: "app-ledger-workspace",
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: "./ledger-workspace.html",
  styleUrl: "./ledger-workspace.scss",
})
export class LedgerWorkspaceComponent {
  private readonly fb = inject(FormBuilder);

  private readonly accountingEntityApi = inject(AccountingEntityApiService);
  private readonly fiscalYearApi = inject(FiscalYearApiService);
  private readonly accountApi = inject(AccountApiService);
  private readonly journalEntryApi = inject(JournalEntryApiService);
  private readonly trialBalanceApi = inject(TrialBalanceApiService);

  readonly accountingEntities = signal<AccountingEntityResponse[]>([]);
  readonly fiscalYears = signal<FiscalYearResponse[]>([]);
  readonly accounts = signal<AccountResponse[]>([]);
  readonly journalEntries = signal<JournalEntryResponse[]>([]);
  readonly trialBalance = signal<TrialBalanceResponse | null>(null);

  readonly selectedAccountingEntityId = signal<string | null>(null);
  readonly selectedFiscalYearId = signal<string | null>(null);

  readonly selectedAccountingEntity = computed(
    () =>
      this.accountingEntities().find(
        (entity) => entity.id === this.selectedAccountingEntityId()
      ) ?? null
  );

  readonly selectedFiscalYear = computed(
    () =>
      this.fiscalYears().find(
        (year) => year.id === this.selectedFiscalYearId()
      ) ?? null
  );

  readonly message = signal<string | null>(null);
  readonly error = signal<string | null>(null);

  readonly accountingEntityForm = this.fb.nonNullable.group({
    name: ["", [Validators.required, Validators.maxLength(255)]],
  });

  readonly fiscalYearForm = this.fb.nonNullable.group({
    year: [new Date().getFullYear(), [Validators.required]],
    startDate: [`${new Date().getFullYear()}-01-01`, [Validators.required]],
    endDate: [`${new Date().getFullYear()}-12-31`, [Validators.required]],
  });

  readonly accountForm = this.fb.nonNullable.group({
    code: ["", [Validators.required, Validators.maxLength(50)]],
    name: ["", [Validators.required, Validators.maxLength(255)]],
    accountType: ["ASSET", [Validators.required]],
    normalBalance: ["DEBIT", [Validators.required]],
    classification: ["CURRENT", [Validators.required]],
  });

  readonly journalEntryForm = this.fb.nonNullable.group({
    entryDate: [new Date().toISOString().slice(0, 10), [Validators.required]],
    entryType: ["STANDARD", [Validators.required]],
    memo: [""],
    lines: this.fb.array([
      this.createJournalLineGroup(),
      this.createJournalLineGroup(),
    ]),
  });

  get journalLines(): FormArray {
    return this.journalEntryForm.controls.lines;
  }

  ngOnInit(): void {
    this.loadAccountingEntities();
  }

  loadAccountingEntities(): void {
    this.clearAlerts();

    this.accountingEntityApi.findAll().subscribe({
      next: (entities) => this.accountingEntities.set(entities),
      error: (error) => this.handleError(error),
    });
  }

  selectAccountingEntity(accountingEntityId: string): void {
    this.selectedAccountingEntityId.set(accountingEntityId);
    this.selectedFiscalYearId.set(null);
    this.fiscalYears.set([]);
    this.accounts.set([]);
    this.journalEntries.set([]);
    this.trialBalance.set(null);

    this.loadFiscalYears(accountingEntityId);
    this.loadAccounts(accountingEntityId);
  }

  selectFiscalYear(fiscalYearId: string): void {
    this.selectedFiscalYearId.set(fiscalYearId);
    this.trialBalance.set(null);

    const accountingEntityId = this.selectedAccountingEntityId();

    if (accountingEntityId) {
      this.loadJournalEntries(accountingEntityId, fiscalYearId);
    }
  }

  createAccountingEntity(): void {
    if (this.accountingEntityForm.invalid) {
      return;
    }

    this.clearAlerts();

    this.accountingEntityApi
      .create(this.accountingEntityForm.getRawValue())
      .subscribe({
        next: (entity) => {
          this.message.set("Accounting entity created.");
          this.accountingEntityForm.reset();
          this.accountingEntities.update((entities) => [...entities, entity]);
          this.selectAccountingEntity(entity.id);
        },
        error: (error) => this.handleError(error),
      });
  }

  createFiscalYear(): void {
    const accountingEntityId = this.selectedAccountingEntityId();

    if (!accountingEntityId || this.fiscalYearForm.invalid) {
      return;
    }

    this.clearAlerts();

    this.fiscalYearApi
      .create({
        accountingEntityId,
        ...this.fiscalYearForm.getRawValue(),
      })
      .subscribe({
        next: (fiscalYear) => {
          this.message.set("Fiscal year created.");
          this.fiscalYears.update((years) => [fiscalYear, ...years]);
          this.selectFiscalYear(fiscalYear.id);
        },
        error: (error) => this.handleError(error),
      });
  }

  createAccount(): void {
    const accountingEntityId = this.selectedAccountingEntityId();

    if (!accountingEntityId || this.accountForm.invalid) {
      return;
    }

    this.clearAlerts();

    this.accountApi
      .create({
        accountingEntityId,
        code: this.accountForm.controls.code.value,
        name: this.accountForm.controls.name.value,
        accountType: this.accountForm.controls.accountType.value as any,
        normalBalance: this.accountForm.controls.normalBalance.value as any,
        classification: this.accountForm.controls.classification.value as any,
      })
      .subscribe({
        next: (account) => {
          this.message.set("Account created.");
          this.accounts.update((accounts) =>
            [...accounts, account].sort((a, b) => a.code.localeCompare(b.code))
          );
          this.accountForm.reset({
            code: "",
            name: "",
            accountType: "ASSET",
            normalBalance: "DEBIT",
            classification: "CURRENT",
          });
        },
        error: (error) => this.handleError(error),
      });
  }

  addJournalLine(): void {
    this.journalLines.push(this.createJournalLineGroup());
  }

  removeJournalLine(index: number): void {
    if (this.journalLines.length <= 2) {
      this.error.set("A journal entry must have at least two lines.");
      return;
    }

    this.journalLines.removeAt(index);
  }

  createJournalEntry(): void {
    const accountingEntityId = this.selectedAccountingEntityId();
    const fiscalYearId = this.selectedFiscalYearId();

    if (!accountingEntityId || !fiscalYearId || this.journalEntryForm.invalid) {
      return;
    }

    this.clearAlerts();

    const raw = this.journalEntryForm.getRawValue();

    this.journalEntryApi
      .create({
        accountingEntityId,
        fiscalYearId,
        entryDate: raw.entryDate,
        entryType: raw.entryType as any,
        memo: raw.memo || null,
        lines: raw.lines.map((line) => ({
          accountId: line.accountId,
          description: line.description || null,
          debitAmount: Number(line.debitAmount ?? 0),
          creditAmount: Number(line.creditAmount ?? 0),
        })),
      })
      .subscribe({
        next: (entry) => {
          this.message.set("Journal entry created.");
          this.journalEntries.update((entries) => [...entries, entry]);
          this.resetJournalEntryForm();
        },
        error: (error) => this.handleError(error),
      });
  }

  postJournalEntry(entry: JournalEntryResponse): void {
    this.clearAlerts();

    this.journalEntryApi.post(entry.id).subscribe({
      next: (postedEntry) => {
        this.message.set("Journal entry posted.");
        this.journalEntries.update((entries) =>
          entries.map((existing) =>
            existing.id === postedEntry.id ? postedEntry : existing
          )
        );
        this.trialBalance.set(null);
      },
      error: (error) => this.handleError(error),
    });
  }

  generateUnadjustedTrialBalance(): void {
    const accountingEntityId = this.selectedAccountingEntityId();
    const fiscalYearId = this.selectedFiscalYearId();

    if (!accountingEntityId || !fiscalYearId) {
      return;
    }

    this.clearAlerts();

    this.trialBalanceApi
      .generateUnadjusted(accountingEntityId, fiscalYearId)
      .subscribe({
        next: (trialBalance) => this.trialBalance.set(trialBalance),
        error: (error) => this.handleError(error),
      });
  }

  private loadFiscalYears(accountingEntityId: string): void {
    this.fiscalYearApi.findByAccountingEntity(accountingEntityId).subscribe({
      next: (years) => this.fiscalYears.set(years),
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
        next: (entries) => this.journalEntries.set(entries),
        error: (error) => this.handleError(error),
      });
  }

  private createJournalLineGroup() {
    return this.fb.nonNullable.group({
      accountId: ["", [Validators.required]],
      description: [""],
      debitAmount: [0, [Validators.min(0)]],
      creditAmount: [0, [Validators.min(0)]],
    });
  }

  private resetJournalEntryForm(): void {
    this.journalEntryForm.reset({
      entryDate: new Date().toISOString().slice(0, 10),
      entryType: "STANDARD",
      memo: "",
    });

    this.journalLines.clear();
    this.journalLines.push(this.createJournalLineGroup());
    this.journalLines.push(this.createJournalLineGroup());
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
