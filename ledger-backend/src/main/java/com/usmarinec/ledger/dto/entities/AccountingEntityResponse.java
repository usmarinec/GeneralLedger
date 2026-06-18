package com.usmarinec.ledger.dto.entities;

import com.usmarinec.ledger.dto.Response;
import java.time.LocalDateTime;
import java.util.UUID;

public record AccountingEntityResponse(UUID id, String name, LocalDateTime createdAt)
    implements Response {}
