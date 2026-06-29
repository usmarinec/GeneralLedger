import { computed, Service, signal } from "@angular/core";

@Service()
export class LedgerContextStore {
  private static readonly ACCOUNTING_ENTITY_ID_KEY =
    "ledger.accountingEntityId";
  private static readonly FISCAL_YEAR_ID_KEY = "ledger.fiscalYearId";

  readonly selectedAccountingEntityId = signal<string | null>(
    sessionStorage.getItem(LedgerContextStore.ACCOUNTING_ENTITY_ID_KEY)
  );

  readonly selectedFiscalYearId = signal<string | null>(
    sessionStorage.getItem(LedgerContextStore.FISCAL_YEAR_ID_KEY)
  );

  readonly hasAccountingEntity = computed(
    () => this.selectedAccountingEntityId() !== null
  );

  readonly hasFiscalYear = computed(() => this.selectedFiscalYearId() !== null);

  readonly hasFullContext = computed(
    () =>
      this.selectedAccountingEntityId() !== null &&
      this.selectedFiscalYearId() !== null
  );

  selectAccountingEntity(accountingEntityId: string): void {
    this.selectedAccountingEntityId.set(accountingEntityId);
    sessionStorage.setItem(
      LedgerContextStore.ACCOUNTING_ENTITY_ID_KEY,
      accountingEntityId
    );

    /*
     * A fiscal year belongs to one accounting entity.
     * If the selected entity changes, the selected fiscal year should be cleared.
     */
    this.clearFiscalYear();
  }

  selectFiscalYear(fiscalYearId: string): void {
    this.selectedFiscalYearId.set(fiscalYearId);
    sessionStorage.setItem(LedgerContextStore.FISCAL_YEAR_ID_KEY, fiscalYearId);
  }

  clearAccountingEntity(): void {
    this.selectedAccountingEntityId.set(null);
    sessionStorage.removeItem(LedgerContextStore.ACCOUNTING_ENTITY_ID_KEY);
    this.clearFiscalYear();
  }

  clearFiscalYear(): void {
    this.selectedFiscalYearId.set(null);
    sessionStorage.removeItem(LedgerContextStore.FISCAL_YEAR_ID_KEY);
  }
}
