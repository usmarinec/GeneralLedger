import { Routes } from "@angular/router";

import { AppShell } from "./layout/app-shell/app-shell";
import { DashboardPage } from "./features/dashboard/dashboard-page";
import { AccountingEntitiesPage } from "./features/accounting-entities/accounting-entities-page";
import { FiscalYearsPage } from "./features/fiscal-years/fiscal-years-page";
import { AccountsPage } from "./features/accounts/accounts-page";
import { JournalEntriesPage } from "./features/journal-entries/journal-entries-page";
import { TrialBalancesPage } from "./features/trial-balances/trial-balances-page";

export const routes: Routes = [
  {
    path: "",
    component: AppShell,
    children: [
      {
        path: "",
        pathMatch: "full",
        redirectTo: "dashboard",
      },
      {
        path: "dashboard",
        component: DashboardPage,
      },
      {
        path: "accounting-entities",
        component: AccountingEntitiesPage,
      },
      {
        path: "fiscal-years",
        component: FiscalYearsPage,
      },
      {
        path: "accounts",
        component: AccountsPage,
      },
      {
        path: "journal-entries",
        component: JournalEntriesPage,
      },
      {
        path: "trial-balances",
        component: TrialBalancesPage,
      },
    ],
  },
];
