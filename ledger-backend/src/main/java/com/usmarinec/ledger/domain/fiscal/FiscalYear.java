package com.usmarinec.ledger.domain.fiscal;

import com.usmarinec.ledger.domain.LedgerDocument;
import com.usmarinec.ledger.domain.entities.AccountingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "fiscal_years",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_fiscal_year_accounting_entity_year",
          columnNames = {"accounting_entity_id", "year"})
    })
public class FiscalYear extends LedgerDocument {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "accounting_entity_id", nullable = false)
  private AccountingEntity accountingEntity;

  @Column(nullable = false)
  private Integer year;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  @Column(nullable = false)
  private boolean closed = false;
}
