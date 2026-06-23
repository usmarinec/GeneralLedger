package com.usmarinec.ledger.dto.error;

import com.usmarinec.ledger.dto.Response;
import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    List<String> details)
    implements Response {}
