package com.usmarinec.ledger.dto.entities;

import com.usmarinec.ledger.dto.CreateRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAccountingEntityRequest(@NotBlank @Size(max = 255) String name)
    implements CreateRequest {}
