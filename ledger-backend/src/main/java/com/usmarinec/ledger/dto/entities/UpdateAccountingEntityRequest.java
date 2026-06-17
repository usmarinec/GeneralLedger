package com.usmarinec.ledger.dto.entities;

import com.usmarinec.ledger.dto.UpdateRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAccountingEntityRequest(@NotBlank @Size(max = 255) String name)
    implements UpdateRequest {}
