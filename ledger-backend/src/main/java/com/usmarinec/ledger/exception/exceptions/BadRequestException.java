package com.usmarinec.ledger.exception.exceptions;

import org.springframework.http.HttpStatus;

public class BadRequestException extends LedgerException {
  public BadRequestException(String message) {
    super(HttpStatus.NOT_FOUND, message);
  }
}
