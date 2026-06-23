package com.usmarinec.ledger.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.usmarinec.ledger.dto.error.ErrorResponse;
import com.usmarinec.ledger.exception.exceptions.ConflictException;
import com.usmarinec.ledger.exception.exceptions.NotFoundException;
import com.usmarinec.ledger.responses.SuccessFailureResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler;
  private HttpServletRequest request;

  @BeforeEach
  void setUp() {
    handler = new GlobalExceptionHandler();

    MockHttpServletRequest mockRequest = new MockHttpServletRequest();
    mockRequest.setRequestURI("/api/test");
    request = mockRequest;
  }

  @Test
  void handleLedgerException_whenNotFound_returnsNotFoundResponse() {
    ResponseEntity<SuccessFailureResponse<ErrorResponse>> responseEntity =
        handler.handleLedgerException(new NotFoundException("Record not found"), request);

    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());

    SuccessFailureResponse<ErrorResponse> body = responseEntity.getBody();

    assertNotNull(body);
    assertEquals(false, body.getSuccess());
    assertEquals("Record not found", body.getMessage());
    assertEquals("Not Found", body.getHttpStatus());

    List<ErrorResponse> items = body.getItems();

    assertNotNull(items);
    assertEquals(1, items.size());
    assertEquals(404, items.get(0).status());
    assertEquals("Not Found", items.get(0).error());
    assertEquals("Record not found", items.get(0).message());
    assertEquals("/api/test", items.get(0).path());
  }

  @Test
  void handleLedgerException_whenConflict_returnsConflictResponse() {
    ResponseEntity<SuccessFailureResponse<ErrorResponse>> responseEntity =
        handler.handleLedgerException(new ConflictException("Duplicate record"), request);

    assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());

    SuccessFailureResponse<ErrorResponse> body = responseEntity.getBody();

    assertNotNull(body);
    assertEquals(false, body.getSuccess());
    assertEquals("Duplicate record", body.getMessage());
    assertEquals("Conflict", body.getHttpStatus());
    assertEquals(409, body.getItems().get(0).status());
  }

  @Test
  void handleResponseStatusException_returnsExceptionStatus() {
    ResponseEntity<SuccessFailureResponse<ErrorResponse>> responseEntity =
        handler.handleResponseStatusException(
            new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request message"), request);

    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

    SuccessFailureResponse<ErrorResponse> body = responseEntity.getBody();

    assertNotNull(body);
    assertEquals(false, body.getSuccess());
    assertEquals("Bad request message", body.getMessage());
    assertEquals("Bad Request", body.getHttpStatus());
    assertEquals(400, body.getItems().get(0).status());
  }

  @Test
  void handleUnexpectedException_returnsInternalServerError() {
    ResponseEntity<SuccessFailureResponse<ErrorResponse>> responseEntity =
        handler.handleUnexpectedException(new RuntimeException("Boom"), request);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());

    SuccessFailureResponse<ErrorResponse> body = responseEntity.getBody();

    assertNotNull(body);
    assertEquals(false, body.getSuccess());
    assertEquals("An unexpected server error occurred", body.getMessage());
    assertEquals("Internal Server Error", body.getHttpStatus());
    assertEquals(500, body.getItems().get(0).status());
  }
}
