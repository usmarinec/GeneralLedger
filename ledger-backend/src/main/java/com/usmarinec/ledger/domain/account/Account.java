package com.usmarinec.ledger.domain.account;

import com.usmarinec.ledger.domain.LedgerDocument;
import com.usmarinec.ledger.domain.entities.Entities;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Data
@Entity
@Table(
    name = "accounts",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_accounts_entity_code",
          columnNames = {"entity_id", "code"})
    })
public class Account extends LedgerDocument {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "entity_id", nullable = false)
  private Entities entity;

  @Column(nullable = false, length = 50)
  private String code;

  @Column(nullable = false, length = 255)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "account_type", nullable = false, length = 50)
  private AccountType accountType;

  @Enumerated(EnumType.STRING)
  @Column(name = "normal_balance", nullable = false, length = 10)
  private NormalBalance normalBalance;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private AccountClassification classification = AccountClassification.NONE;

  @Column(nullable = false)
  private boolean active = true;
}
