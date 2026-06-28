import { CommonModule } from "@angular/common";
import { Component, computed, effect, inject, signal } from "@angular/core";
import {
  FormArray,
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";

import { AccountApiService } from "../../core/services/account-api-service";
import { JournalEntryApiService } from "../../core/services/journal-entry-api-service";
import { LedgerContextStore } from "../../core/state/ledger-context.store";
import { LedgerContextSelector } from "../../shared/components/ledger-context-selector/ledger-context-selector";
import { AccountResponse } from "../../shared/models/account.model";
import {
  CreateJournalEntryRequest,
  JournalEntryResponse,
  JournalEntryType,
  UpdateJournalEntryRequest,
} from "../../shared/models/journal-entry.model";

@Component({
  selector: "app-journal-entries-page",
  imports: [CommonModule, ReactiveFormsModule, LedgerContextSelector],
  templateUrl: "./journal-entries-page.html",
  styleUrl: "./journal-entries-page.scss",
})
export class JournalEntriesPage {
  private readonly fb = inject(FormBuilder);
  private readonly accountApi = inject(AccountApiService);
  private readonly journalEntryApi = inject(JournalEntryApiService);

  readonly context = inject(LedgerContextStore);

  readonly accounts = signal<AccountResponse[]>([]);
  readonly journalEntries = signal<JournalEntryResponse[]>([]);
  readonly editingJournalEntryId = signal<string | null>(null);
  readonly expandedJournalEntryId = signal<string | null>(null);
  readonly message = signal<string | null>(null);
  readonly error = signal<string | null>(null);

  readonly canCreateJournalEntry = computed(
    () => this.context.hasFullContext() && this.accounts().length >= 2
  );

  readonly debitTotal = computed(() =>
    this.journalEntryForm
      .getRawValue()
      .lines.reduce((sum, line) => sum + Number(line.debitAmount ?? 0), 0)
  );

  readonly creditTotal = computed(() =>
    this.journalEntryForm
      .getRawValue()
      .lines.reduce((sum, line) => sum + Number(line.creditAmount ?? 0), 0)
  );

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

  constructor() {
    effect(() => {
      const accountingEntityId = this.context.selectedAccountingEntityId();
      const fiscalYearId = this.context.selectedFiscalYearId();

      this.cancelEdit();
      this.message.set(null);
      this.error.set(null);

      if (accountingEntityId) {
        this.loadAccounts(accountingEntityId);
      } else {
        this.accounts.set([]);
      }

      if (accountingEntityId && fiscalYearId) {
        this.loadJournalEntries(accountingEntityId, fiscalYearId);
      } else {
        this.journalEntries.set([]);
      }
    });
  }

  saveJournalEntry(): void {
    const accountingEntityId = this.context.selectedAccountingEntityId();
    const fiscalYearId = this.context.selectedFiscalYearId();

    if (!accountingEntityId || !fiscalYearId || this.journalEntryForm.invalid) {
      return;
    }

    const editingJournalEntryId = this.editingJournalEntryId();
    const raw = this.journalEntryForm.getRawValue();

    if (editingJournalEntryId) {
      const request: UpdateJournalEntryRequest = {
        fiscalYearId,
        entryDate: raw.entryDate,
        entryType: raw.entryType as JournalEntryType,
        memo: raw.memo || null,
        lines: raw.lines.map((line) => ({
          accountId: line.accountId,
          description: line.description || null,
          debitAmount: Number(line.debitAmount ?? 0),
          creditAmount: Number(line.creditAmount ?? 0),
        })),
      };

      this.updateJournalEntry(editingJournalEntryId, request);
    } else {
      const request: CreateJournalEntryRequest = {
        accountingEntityId,
        fiscalYearId,
        entryDate: raw.entryDate,
        entryType: raw.entryType as JournalEntryType,
        memo: raw.memo || null,
        lines: raw.lines.map((line) => ({
          accountId: line.accountId,
          description: line.description || null,
          debitAmount: Number(line.debitAmount ?? 0),
          creditAmount: Number(line.creditAmount ?? 0),
        })),
      };

      this.createJournalEntry(request);
    }
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

  startEdit(entry: JournalEntryResponse): void {
    if (entry.status !== "DRAFT") {
      this.error.set("Only draft journal entries can be edited.");
      return;
    }

    this.editingJournalEntryId.set(entry.id);

    this.journalEntryForm.controls.entryDate.setValue(entry.entryDate);
    this.journalEntryForm.controls.entryType.setValue(entry.entryType);
    this.journalEntryForm.controls.memo.setValue(entry.memo ?? "");

    this.journalLines.clear();

    for (const line of entry.lines) {
      this.journalLines.push(
        this.fb.nonNullable.group({
          accountId: [line.accountId, [Validators.required]],
          description: [line.description ?? ""],
          debitAmount: [Number(line.debitAmount ?? 0), [Validators.min(0)]],
          creditAmount: [Number(line.creditAmount ?? 0), [Validators.min(0)]],
        })
      );
    }
  }

  cancelEdit(): void {
    this.editingJournalEntryId.set(null);
    this.resetJournalEntryForm();
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
      },
      error: (error) => this.handleError(error),
    });
  }

  toggleExpanded(entry: JournalEntryResponse): void {
    this.expandedJournalEntryId.update((current) =>
      current === entry.id ? null : entry.id
    );
  }

  refresh(): void {
    const accountingEntityId = this.context.selectedAccountingEntityId();
    const fiscalYearId = this.context.selectedFiscalYearId();

    if (accountingEntityId) {
      this.loadAccounts(accountingEntityId);
    }

    if (accountingEntityId && fiscalYearId) {
      this.loadJournalEntries(accountingEntityId, fiscalYearId);
    }
  }

  private createJournalEntry(request: CreateJournalEntryRequest): void {
    this.clearAlerts();

    this.journalEntryApi.create(request).subscribe({
      next: (entry) => {
        this.message.set("Journal entry created.");
        this.journalEntries.update((entries) => [...entries, entry]);
        this.resetJournalEntryForm();
      },
      error: (error) => this.handleError(error),
    });
  }

  private updateJournalEntry(
    id: string,
    request: UpdateJournalEntryRequest
  ): void {
    this.clearAlerts();

    this.journalEntryApi.update(id, request).subscribe({
      next: (updatedEntry) => {
        this.message.set("Journal entry updated.");
        this.journalEntries.update((entries) =>
          entries.map((entry) =>
            entry.id === updatedEntry.id ? updatedEntry : entry
          )
        );
        this.cancelEdit();
      },
      error: (error) => this.handleError(error),
    });
  }

  private loadAccounts(accountingEntityId: string): void {
    this.accountApi.findByAccountingEntity(accountingEntityId).subscribe({
      next: (accounts) =>
        this.accounts.set(accounts.filter((account) => account.active)),
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
