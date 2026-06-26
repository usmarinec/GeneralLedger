package com.usmarinec.ledger.repositories.trialbalance;

import com.usmarinec.ledger.domain.journal.JournalEntryLine;
import com.usmarinec.ledger.domain.journal.JournalEntryStatus;
import com.usmarinec.ledger.domain.journal.JournalEntryType;
import com.usmarinec.ledger.repositories.LedgerRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrialBalanceRepository extends LedgerRepository<JournalEntryLine> {
  @Query(
      """
      SELECT
        account.id AS accountId,
        account.code AS accountCode,
        account.name AS accountName,
        account.accountType AS accountType,
        account.normalBalance AS normalBalance,
        COALESCE(SUM(line.debitAmount), 0) AS totalDebits,
        COALESCE(SUM(line.creditAmount), 0) AS totalCredits
      FROM JournalEntryLine line
      JOIN line.journalEntry journalEntry
      JOIN line.account account
      WHERE journalEntry.accountingEntity.id = :accountingEntityId
        AND journalEntry.fiscalYear.id = :fiscalYearId
        AND journalEntry.status = :status
        AND journalEntry.entryType = :entryType
      GROUP BY
        account.id,
        account.code,
        account.name,
        account.accountType,
        account.normalBalance
      ORDER BY account.code
      """)
  List<TrialBalanceLineProjection> findTrialBalanceLines(
      @Param("accountingEntityId") UUID accountingEntityId,
      @Param("fiscalYearId") UUID fiscalYearId,
      @Param("status") JournalEntryStatus status,
      @Param("entryType") JournalEntryType entryType);
}
