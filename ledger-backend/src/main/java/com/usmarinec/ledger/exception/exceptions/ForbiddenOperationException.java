package com.usmarinec.ledger.exception.exceptions;

import org.springframework.http.HttpStatus;

public class ForbiddenOperationException extends LedgerException {
  public ForbiddenOperationException(String message) {
    super(HttpStatus.FORBIDDEN, message);
  }
}
