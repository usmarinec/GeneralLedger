package com.usmarinec.ledger.exceptions;

import org.springframework.http.HttpStatus;

public abstract class LedgerException extends RuntimeException {
  private final HttpStatus status;

  protected LedgerException(HttpStatus status, String message) {
    super(message);
    this.status = status;
  }

  protected LedgerException(HttpStatus status, String message, Throable cause) {
    super(message, cause);
    this.status = status;
  }

  public HttpStatus getStatus() {
    return this.status;
  }
}
