package com.usmarinec.ledger.domain.entities;

import com.usmarinec.ledger.domain.LedgerDocument;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "entities")
public class AccountingEntity extends LedgerDocument {
  @Column(nullable = false, length = 255)
  private String name;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Override
  @PrePersist
  protected void prePersist() {
    super.prePersist();

    if (this.createdAt == null) {
      this.createdAt = LocalDateTime.now();
    }
  }
}
