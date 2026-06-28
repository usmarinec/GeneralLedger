import { CommonModule } from "@angular/common";
import { Component, inject, signal } from "@angular/core";
import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";

import { AccountingEntityApiService } from "../../core/services/accounting-entity-api-service";
import { LedgerContextStore } from "../../core/state/ledger-context.store";
import {
  AccountingEntityResponse,
  CreateAccountingEntityRequest,
  UpdateAccountingEntityRequest,
} from "../../shared/models/accounting-entity.model";

@Component({
  selector: "app-accounting-entities-page",
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: "./accounting-entities-page.html",
  styleUrl: "./accounting-entities-page.scss",
})
export class AccountingEntitiesPage {
  private readonly fb = inject(FormBuilder);
  private readonly accountingEntityApi = inject(AccountingEntityApiService);

  readonly context = inject(LedgerContextStore);

  readonly accountingEntities = signal<AccountingEntityResponse[]>([]);
  readonly editingEntityId = signal<string | null>(null);
  readonly message = signal<string | null>(null);
  readonly error = signal<string | null>(null);

  readonly accountingEntityForm = this.fb.nonNullable.group({
    name: ["", [Validators.required, Validators.maxLength(255)]],
  });

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

  saveAccountingEntity(): void {
    if (this.accountingEntityForm.invalid) {
      return;
    }

    const editingEntityId = this.editingEntityId();
    const request = this.accountingEntityForm.getRawValue();

    if (editingEntityId) {
      this.updateAccountingEntity(editingEntityId, request);
    } else {
      this.createAccountingEntity(request);
    }
  }

  selectAccountingEntity(entity: AccountingEntityResponse): void {
    this.context.selectAccountingEntity(entity.id);
    this.message.set(`Selected accounting entity: ${entity.name}`);
    this.error.set(null);
  }

  startEdit(entity: AccountingEntityResponse): void {
    this.editingEntityId.set(entity.id);
    this.accountingEntityForm.setValue({
      name: entity.name,
    });
  }

  cancelEdit(): void {
    this.editingEntityId.set(null);
    this.accountingEntityForm.reset({
      name: "",
    });
  }

  deleteAccountingEntity(entity: AccountingEntityResponse): void {
    this.clearAlerts();

    this.accountingEntityApi.delete(entity.id).subscribe({
      next: () => {
        this.message.set("Accounting entity deleted.");
        this.accountingEntities.update((entities) =>
          entities.filter((existing) => existing.id !== entity.id)
        );

        if (this.context.selectedAccountingEntityId() === entity.id) {
          this.context.clearAccountingEntity();
        }

        if (this.editingEntityId() === entity.id) {
          this.cancelEdit();
        }
      },
      error: (error) => this.handleError(error),
    });
  }

  private createAccountingEntity(request: CreateAccountingEntityRequest): void {
    this.clearAlerts();

    this.accountingEntityApi.create(request).subscribe({
      next: (entity) => {
        this.message.set("Accounting entity created.");
        this.accountingEntities.update((entities) =>
          [...entities, entity].sort((a, b) => a.name.localeCompare(b.name))
        );
        this.accountingEntityForm.reset({ name: "" });
        this.context.selectAccountingEntity(entity.id);
      },
      error: (error) => this.handleError(error),
    });
  }

  private updateAccountingEntity(
    id: string,
    request: UpdateAccountingEntityRequest
  ): void {
    this.clearAlerts();

    this.accountingEntityApi.update(id, request).subscribe({
      next: (updatedEntity) => {
        this.message.set("Accounting entity updated.");
        this.accountingEntities.update((entities) =>
          entities
            .map((entity) =>
              entity.id === updatedEntity.id ? updatedEntity : entity
            )
            .sort((a, b) => a.name.localeCompare(b.name))
        );
        this.cancelEdit();
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
