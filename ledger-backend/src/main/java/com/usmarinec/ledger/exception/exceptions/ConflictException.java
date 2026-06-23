package com.usmarinec.ledger.exception.exceptions;

import org.springframework.http.HttpStatus;

public class ConflictException extends LedgerException {
  public ConflictException(String message) {
    super(HttpStatus.CONFLICT, message);
  }
}
