package com.usmarinec.ledger.exception.exceptions;

import org.springframework.http.HttpStatus;

public class NotFoundException extends LedgerException {
  public NotFoundException(String message) {
    super(HttpStatus.NOT_FOUND, message);
  }
}
