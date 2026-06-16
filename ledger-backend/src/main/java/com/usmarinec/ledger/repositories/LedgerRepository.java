package com.usmarinec.ledger.repositories;

import com.usmarinec.ledger.domain.LedgerDocument;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerRepository<T extends LedgerDocument> extends JpaRepository<T, UUID> {}
