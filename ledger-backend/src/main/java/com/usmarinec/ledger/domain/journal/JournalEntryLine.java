package com.usmarinec.ledger.domain.journal;

import com.usmarinec.ledger.domain.LedgerDocument;
import com.usmarinec.ledger.domain.account.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(
    name = "journal_entry_lines",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_journal_line_number",
          columnNames = {"journal_entry_id", "line_number"})
    })
public class JournalEntryLine extends LedgerDocument {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "journal_entry_id", nullable = false)
  private JournalEntry journalEntry;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "account_id", nullable = false)
  private Account account;

  @Column(name = "line_number", nullable = false)
  private Integer lineNumber;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "debit_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal debitAmount = BigDecimal.ZERO;

  @Column(name = "credit_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal creditAmount = BigDecimal.ZERO;

  @PrePersist
  void prePersist() {
    if (this.id == null) {
      this.id = UUID.randomUUID();
    }

    if (this.debitAmount == null) {
      debitAmount = BigDecimal.ZERO;
    }

    if (this.creditAmount == null) {
      this.creditAmount = BigDecimal.ZERO;
    }
  }
}
