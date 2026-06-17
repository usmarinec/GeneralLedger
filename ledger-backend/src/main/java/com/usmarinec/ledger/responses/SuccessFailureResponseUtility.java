package com.usmarinec.ledger.responses;

import com.usmarinec.ledger.dto.Response;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class SuccessFailureResponseUtility<ResponseT extends Response> {
  /**
   * Creates a ResponseEntity of type SuccessFailureResponse.
   *
   * @param success boolean value success
   * @param message string value response message
   * @param status HttpStatus value response code
   * @param type T requestBody object
   * @return ResponseEntity
   */
  public static <ResponseT extends Response> ResponseEntity<SuccessFailureResponse<ResponseT>> createSuccessFailureResponse(
      boolean success, String message, HttpStatus status, ResponseT type) {
    if (success) {
      return new ResponseEntity<>(
          SuccessFailureResponse.success(message, status.getReasonPhrase(), type), status);
    } else {
      return new ResponseEntity<>(
          SuccessFailureResponse.failure(message, status.getReasonPhrase()), status);
    }
  }

  /**
   * Creates a ResponseEntity of type SuccessFailureResponse.
   *
   * @param success boolean value success
   * @param message string value response message
   * @param status HttpStatus value response code
   * @param types List T requestBody list object
   * @return ResponseEntity
   */
  public static <ResponseT extends Response> ResponseEntity<SuccessFailureResponse<ResponseT>> createSuccessFailureResponse(
      boolean success, String message, HttpStatus status, List<ResponseT> types) {
    if (success) {
      return new ResponseEntity<>(
          SuccessFailureResponse.success(message, status.getReasonPhrase(), types), status);
    } else {
      return new ResponseEntity<>(
          SuccessFailureResponse.failure(message, status.getReasonPhrase()), status);
    }
  }

  /**
   * Creates a ResponseEntity of type SuccessFailureResponse.
   *
   * @param success boolean value success
   * @param message string value response message
   * @param status HttpStatus value response code
   * @return ResponseEntity
   */
  public static <ResponseT extends Response> ResponseEntity<SuccessFailureResponse<ResponseT>> createSuccessFailureResponse(
      boolean success, String message, HttpStatus status) {
    if (success) {
      return new ResponseEntity<>(
          SuccessFailureResponse.success(message, status.getReasonPhrase()), status);
    } else {
      return new ResponseEntity<>(
          SuccessFailureResponse.failure(message, status.getReasonPhrase()), status);
    }
  }
}
