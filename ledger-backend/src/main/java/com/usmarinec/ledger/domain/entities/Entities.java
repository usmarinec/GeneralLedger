package com.usmarinec.ledger.domain.entities;

import com.usmarinec.ledger.domain.LedgerDocument;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "entities")
public class Entities extends LedgerDocument {
  @Column(nullable = false, length = 255)
  private String name;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  void prePersist() {
    if (this.id == null) {
      this.id = UUID.randomUUID();
    }

    if (this.createdAt == null) {
      this.createdAt = LocalDateTime.now();
    }
  }
}
