import { CommonModule } from "@angular/common";
import { Component, effect, inject, signal } from "@angular/core";
import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";

import { FiscalYearApiService } from "../../core/services/fiscal-year-api-service";
import { LedgerContextStore } from "../../core/state/ledger-context.store";
import { LedgerContextSelector } from "../../shared/components/ledger-context-selector/ledger-context-selector";
import {
  CreateFiscalYearRequest,
  FiscalYearResponse,
  UpdateFiscalYearRequest,
} from "../../shared/models/fiscal-year.model";

@Component({
  selector: "app-fiscal-years-page",
  imports: [CommonModule, ReactiveFormsModule, LedgerContextSelector],
  templateUrl: "./fiscal-years-page.html",
  styleUrl: "./fiscal-years-page.scss",
})
export class FiscalYearsPage {
  private readonly fb = inject(FormBuilder);
  private readonly fiscalYearApi = inject(FiscalYearApiService);

  readonly context = inject(LedgerContextStore);

  readonly fiscalYears = signal<FiscalYearResponse[]>([]);
  readonly editingFiscalYearId = signal<string | null>(null);
  readonly message = signal<string | null>(null);
  readonly error = signal<string | null>(null);

  readonly fiscalYearForm = this.fb.nonNullable.group({
    year: [new Date().getFullYear(), [Validators.required]],
    startDate: [`${new Date().getFullYear()}-01-01`, [Validators.required]],
    endDate: [`${new Date().getFullYear()}-12-31`, [Validators.required]],
    closed: [false],
  });

  constructor() {
    effect(() => {
      const accountingEntityId = this.context.selectedAccountingEntityId();

      this.cancelEdit();
      this.message.set(null);
      this.error.set(null);

      if (accountingEntityId) {
        this.loadFiscalYears(accountingEntityId);
      } else {
        this.fiscalYears.set([]);
      }
    });
  }

  saveFiscalYear(): void {
    const accountingEntityId = this.context.selectedAccountingEntityId();

    if (!accountingEntityId || this.fiscalYearForm.invalid) {
      return;
    }

    const editingFiscalYearId = this.editingFiscalYearId();
    const raw = this.fiscalYearForm.getRawValue();

    if (editingFiscalYearId) {
      const request: UpdateFiscalYearRequest = {
        accountingEntityId,
        year: raw.year,
        startDate: raw.startDate,
        endDate: raw.endDate,
        closed: raw.closed,
      };

      this.updateFiscalYear(editingFiscalYearId, request);
    } else {
      const request: CreateFiscalYearRequest = {
        accountingEntityId,
        year: raw.year,
        startDate: raw.startDate,
        endDate: raw.endDate,
      };

      this.createFiscalYear(request);
    }
  }

  selectFiscalYear(fiscalYear: FiscalYearResponse): void {
    this.context.selectFiscalYear(fiscalYear.id);
    this.message.set(`Selected fiscal year: ${fiscalYear.year}`);
    this.error.set(null);
  }

  startEdit(fiscalYear: FiscalYearResponse): void {
    this.editingFiscalYearId.set(fiscalYear.id);
    this.fiscalYearForm.setValue({
      year: fiscalYear.year,
      startDate: fiscalYear.startDate,
      endDate: fiscalYear.endDate,
      closed: fiscalYear.closed,
    });
  }

  cancelEdit(): void {
    this.editingFiscalYearId.set(null);

    const currentYear = new Date().getFullYear();

    this.fiscalYearForm.reset({
      year: currentYear,
      startDate: `${currentYear}-01-01`,
      endDate: `${currentYear}-12-31`,
      closed: false,
    });
  }

  refresh(): void {
    const accountingEntityId = this.context.selectedAccountingEntityId();

    if (accountingEntityId) {
      this.loadFiscalYears(accountingEntityId);
    }
  }

  private createFiscalYear(request: CreateFiscalYearRequest): void {
    this.clearAlerts();

    this.fiscalYearApi.create(request).subscribe({
      next: (fiscalYear) => {
        this.message.set("Fiscal year created.");
        this.fiscalYears.update((years) =>
          [fiscalYear, ...years].sort((a, b) => b.year - a.year)
        );
        this.context.selectFiscalYear(fiscalYear.id);
        this.cancelEdit();
      },
      error: (error) => this.handleError(error),
    });
  }

  private updateFiscalYear(id: string, request: UpdateFiscalYearRequest): void {
    this.clearAlerts();

    this.fiscalYearApi.update(id, request).subscribe({
      next: (updatedFiscalYear) => {
        this.message.set("Fiscal year updated.");
        this.fiscalYears.update((years) =>
          years
            .map((year) =>
              year.id === updatedFiscalYear.id ? updatedFiscalYear : year
            )
            .sort((a, b) => b.year - a.year)
        );
        this.cancelEdit();
      },
      error: (error) => this.handleError(error),
    });
  }

  private loadFiscalYears(accountingEntityId: string): void {
    this.fiscalYearApi.findByAccountingEntity(accountingEntityId).subscribe({
      next: (years) => this.fiscalYears.set(years),
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
