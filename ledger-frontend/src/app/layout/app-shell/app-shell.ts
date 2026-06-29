import { Component, signal } from "@angular/core";
import { RouterLink, RouterLinkActive, RouterOutlet } from "@angular/router";

import { LedgerContextSelector } from "../../shared/components/ledger-context-selector/ledger-context-selector";

@Component({
  selector: "app-shell",
  imports: [RouterOutlet, RouterLink, RouterLinkActive, LedgerContextSelector],
  templateUrl: "./app-shell.html",
  styleUrl: "./app-shell.scss",
})
export class AppShell {
  private static readonly SIDEBAR_COLLAPSED_KEY = "ledger.sidebarCollapsed";

  readonly sidebarCollapsed = signal(
    localStorage.getItem(AppShell.SIDEBAR_COLLAPSED_KEY) === "true"
  );

  toggleSidebar(): void {
    this.sidebarCollapsed.update((collapsed) => {
      const nextValue = !collapsed;
      localStorage.setItem(AppShell.SIDEBAR_COLLAPSED_KEY, String(nextValue));
      return nextValue;
    });
  }
}
