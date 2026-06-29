import { CommonModule } from "@angular/common";
import { Component, effect, inject, signal } from "@angular/core";
import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";

import { AccountApiService } from "../../core/services/account-api-service";
import { LedgerContextStore } from "../../core/state/ledger-context.store";
import { LedgerContextSelector } from "../../shared/components/ledger-context-selector/ledger-context-selector";
import {
  AccountClassification,
  AccountResponse,
  AccountType,
  CreateAccountRequest,
  NormalBalance,
  UpdateAccountRequest,
} from "../../shared/models/account.model";

@Component({
  selector: "app-accounts-page",
  imports: [CommonModule, ReactiveFormsModule, LedgerContextSelector],
  templateUrl: "./accounts-page.html",
  styleUrl: "./accounts-page.scss",
})
export class AccountsPage {
  private readonly fb = inject(FormBuilder);
  private readonly accountApi = inject(AccountApiService);

  readonly context = inject(LedgerContextStore);

  readonly accounts = signal<AccountResponse[]>([]);
  readonly editingAccountId = signal<string | null>(null);
  readonly message = signal<string | null>(null);
  readonly error = signal<string | null>(null);

  readonly accountTypes: AccountType[] = [
    "ASSET",
    "LIABILITY",
    "EQUITY",
    "REVENUE",
    "EXPENSE",
  ];
  readonly normalBalances: NormalBalance[] = ["DEBIT", "CREDIT"];
  readonly classifications: AccountClassification[] = [
    "CURRENT",
    "NON_CURRENT",
    "NONE",
  ];

  readonly accountForm = this.fb.nonNullable.group({
    code: ["", [Validators.required, Validators.maxLength(50)]],
    name: ["", [Validators.required, Validators.maxLength(255)]],
    accountType: ["ASSET", [Validators.required]],
    normalBalance: ["DEBIT", [Validators.required]],
    classification: ["CURRENT", [Validators.required]],
    active: [true],
  });

  constructor() {
    effect(() => {
      const accountingEntityId = this.context.selectedAccountingEntityId();

      this.cancelEdit();
      this.message.set(null);
      this.error.set(null);

      if (accountingEntityId) {
        this.loadAccounts(accountingEntityId);
      } else {
        this.accounts.set([]);
      }
    });
  }

  saveAccount(): void {
    const accountingEntityId = this.context.selectedAccountingEntityId();

    if (!accountingEntityId || this.accountForm.invalid) {
      return;
    }

    const editingAccountId = this.editingAccountId();
    const raw = this.accountForm.getRawValue();

    if (editingAccountId) {
      const request: UpdateAccountRequest = {
        accountingEntityId,
        code: raw.code,
        name: raw.name,
        accountType: raw.accountType as AccountType,
        normalBalance: raw.normalBalance as NormalBalance,
        classification: raw.classification as AccountClassification,
        active: raw.active,
      };

      this.updateAccount(editingAccountId, request);
    } else {
      const request: CreateAccountRequest = {
        accountingEntityId,
        code: raw.code,
        name: raw.name,
        accountType: raw.accountType as AccountType,
        normalBalance: raw.normalBalance as NormalBalance,
        classification: raw.classification as AccountClassification,
      };

      this.createAccount(request);
    }
  }

  startEdit(account: AccountResponse): void {
    this.editingAccountId.set(account.id);
    this.accountForm.setValue({
      code: account.code,
      name: account.name,
      accountType: account.accountType,
      normalBalance: account.normalBalance,
      classification: account.classification,
      active: account.active,
    });
  }

  cancelEdit(): void {
    this.editingAccountId.set(null);
    this.accountForm.reset({
      code: "",
      name: "",
      accountType: "ASSET",
      normalBalance: "DEBIT",
      classification: "CURRENT",
      active: true,
    });
  }

  refresh(): void {
    const accountingEntityId = this.context.selectedAccountingEntityId();

    if (accountingEntityId) {
      this.loadAccounts(accountingEntityId);
    }
  }

  private createAccount(request: CreateAccountRequest): void {
    this.clearAlerts();

    this.accountApi.create(request).subscribe({
      next: (account) => {
        this.message.set("Account created.");
        this.accounts.update((accounts) =>
          [...accounts, account].sort((a, b) => a.code.localeCompare(b.code))
        );
        this.cancelEdit();
      },
      error: (error) => this.handleError(error),
    });
  }

  private updateAccount(id: string, request: UpdateAccountRequest): void {
    this.clearAlerts();

    this.accountApi.update(id, request).subscribe({
      next: (updatedAccount) => {
        this.message.set("Account updated.");
        this.accounts.update((accounts) =>
          accounts
            .map((account) =>
              account.id === updatedAccount.id ? updatedAccount : account
            )
            .sort((a, b) => a.code.localeCompare(b.code))
        );
        this.cancelEdit();
      },
      error: (error) => this.handleError(error),
    });
  }

  private loadAccounts(accountingEntityId: string): void {
    this.accountApi.findByAccountingEntity(accountingEntityId).subscribe({
      next: (accounts) => this.accounts.set(accounts),
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
