import { CommonModule } from "@angular/common";
import { Component, effect, inject, signal } from "@angular/core";
import { FormControl, ReactiveFormsModule } from "@angular/forms";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";

import { AccountingEntityApiService } from "../../../core/services/accounting-entity-api-service";
import { FiscalYearApiService } from "../../../core/services/fiscal-year-api-service";
import { LedgerContextStore } from "../../../core/state/ledger-context.store";

import { AccountingEntityResponse } from "../../models/accounting-entity.model";
import { FiscalYearResponse } from "../../models/fiscal-year.model";

@Component({
  selector: "app-ledger-context-selector",
  imports: [CommonModule, ReactiveFormsModule],
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

  readonly accountingEntityControl = new FormControl<string>("", {
    nonNullable: true,
  });

  readonly fiscalYearControl = new FormControl<string>(
    {
      value: "",
      disabled: true,
    },
    {
      nonNullable: true,
    }
  );

  constructor() {
    this.loadAccountingEntities();

    this.accountingEntityControl.valueChanges
      .pipe(takeUntilDestroyed())
      .subscribe((accountingEntityId) => {
        if (!accountingEntityId) {
          this.context.clearAccountingEntity();
          this.fiscalYears.set([]);
          this.fiscalYearControl.setValue("", { emitEvent: false });
          this.fiscalYearControl.disable({ emitEvent: false });
          return;
        }

        if (accountingEntityId !== this.context.selectedAccountingEntityId()) {
          this.context.selectAccountingEntity(accountingEntityId);
        }
      });

    this.fiscalYearControl.valueChanges
      .pipe(takeUntilDestroyed())
      .subscribe((fiscalYearId) => {
        if (!fiscalYearId) {
          this.context.clearFiscalYear();
          return;
        }

        if (fiscalYearId !== this.context.selectedFiscalYearId()) {
          this.context.selectFiscalYear(fiscalYearId);
        }
      });

    effect(() => {
      const accountingEntityId = this.context.selectedAccountingEntityId();

      this.accountingEntityControl.setValue(accountingEntityId ?? "", {
        emitEvent: false,
      });

      if (accountingEntityId) {
        this.fiscalYearControl.enable({ emitEvent: false });
        this.loadFiscalYears(accountingEntityId);
      } else {
        this.fiscalYears.set([]);
        this.fiscalYearControl.setValue("", { emitEvent: false });
        this.fiscalYearControl.disable({ emitEvent: false });
      }
    });

    effect(() => {
      const fiscalYearId = this.context.selectedFiscalYearId();

      this.fiscalYearControl.setValue(fiscalYearId ?? "", {
        emitEvent: false,
      });
    });
  }

  private loadAccountingEntities(): void {
    this.accountingEntityApi.findAll().subscribe({
      next: (entities) => {
        this.accountingEntities.set(entities);
        this.syncAccountingEntityControlWithStore();
      },
      error: (error) => this.handleError(error),
    });
  }

  private loadFiscalYears(accountingEntityId: string): void {
    this.fiscalYearApi.findByAccountingEntity(accountingEntityId).subscribe({
      next: (fiscalYears) => {
        this.fiscalYears.set(fiscalYears);
        this.syncFiscalYearControlWithStore();
      },
      error: (error) => this.handleError(error),
    });
  }

  private syncAccountingEntityControlWithStore(): void {
    const selectedAccountingEntityId =
      this.context.selectedAccountingEntityId();

    if (!selectedAccountingEntityId) {
      this.accountingEntityControl.setValue("", { emitEvent: false });
      return;
    }

    const selectedEntityStillExists = this.accountingEntities().some(
      (entity) => entity.id === selectedAccountingEntityId
    );

    if (selectedEntityStillExists) {
      this.accountingEntityControl.setValue(selectedAccountingEntityId, {
        emitEvent: false,
      });
    } else {
      this.context.clearAccountingEntity();
      this.accountingEntityControl.setValue("", { emitEvent: false });
    }
  }

  private syncFiscalYearControlWithStore(): void {
    const selectedFiscalYearId = this.context.selectedFiscalYearId();

    if (!selectedFiscalYearId) {
      this.fiscalYearControl.setValue("", { emitEvent: false });
      return;
    }

    const selectedFiscalYearStillExists = this.fiscalYears().some(
      (fiscalYear) => fiscalYear.id === selectedFiscalYearId
    );

    if (selectedFiscalYearStillExists) {
      this.fiscalYearControl.setValue(selectedFiscalYearId, {
        emitEvent: false,
      });
    } else {
      this.context.clearFiscalYear();
      this.fiscalYearControl.setValue("", { emitEvent: false });
    }
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
