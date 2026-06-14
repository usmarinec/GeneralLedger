package com.usmarinec.ledger.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.util.UUID;
import lombok.Data;

@Data
@MappedSuperclass
public abstract class LedgerDocument {
  @Id
  @Column(nullable = false, updatable = false)
  protected UUID id;
}
