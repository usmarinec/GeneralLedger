package com.usmarinec.ledger.domain.fiscal;

import com.usmarinec.ledger.domain.LedgerDocument;
import com.usmarinec.ledger.domain.entities.Entities;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(
    name = "fiscal_years",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_fiscal_year_entity_year",
          columnNames = {"entity_id", "year"})
    })
public class FiscalYear extends LedgerDocument {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "entity_id", nullable = false)
  private Entities entity;

  @Column(nullable = false)
  private Integer year;

  @Column(name = "start_date", nullable = false)
  private LocalDateTime startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDateTime endDate;

  @Column(nullable = false)
  private boolean closed = false;

  @PrePersist
  void prePersist() {
    if (this.id == null) {
      this.id = UUID.randomUUID();
    }
  }
}
