package com.usmarinec.ledger.domain.journal;

import com.usmarinec.ledger.domain.LedgerDocument;
import com.usmarinec.ledger.domain.entities.Entities;
import com.usmarinec.ledger.domain.fiscal.FiscalYear;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "journal_entries")
public class JournalEntry extends LedgerDocument {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "entity_id", nullable = false)
  private Entities entity;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "fiscal_year_id", nullable = false)
  private FiscalYear fiscalYear;

  @Column(name = "entry_date", nullable = false)
  private LocalDate entryDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "entry_type", nullable = false, length = 50)
  private JournalEntryType entryType = JournalEntryType.STANDARD;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private JournalEntryStatus status = JournalEntryStatus.DRAFT;

  @Column(columnDefinition = "TEXT")
  private String memo;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "posted_at")
  private LocalDateTime postedAt;

  @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<JournalEntryLine> lines = new ArrayList<>();

  @PrePersist
  void prePersist() {
    if (this.id == null) {
      this.id = UUID.randomUUID();
    }

    if (this.createdAt == null) {
      this.createdAt = LocalDateTime.now();
    }
  }

  public void addLine(JournalEntryLine line) {
    this.lines.add(line);
    line.setJournalEntry(this);
  }

  public void removeLine(JournalEntryLine line) {
    this.lines.remove(line);
    line.setJournalEntry(null);
  }
}
