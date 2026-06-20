package com.usmarinec.ledger.responses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.usmarinec.ledger.dto.Response;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class SuccessFailureResponseUtilityTest {

  @Test
  void createSuccessFailureResponse_withSingleItemAndSuccessTrue_returnsSuccessResponse() {
    TestResponse item = new TestResponse(UUID.randomUUID(), "Test Entity");

    ResponseEntity<SuccessFailureResponse<TestResponse>> responseEntity =
        SuccessFailureResponseUtility.createSuccessFailureResponse(
            true, "Record created", HttpStatus.CREATED, item);

    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

    SuccessFailureResponse<TestResponse> body = responseEntity.getBody();

    assertNotNull(body);
    assertEquals(true, body.getSuccess());
    assertEquals("Record created", body.getMessage());
    assertEquals("Created", body.getHttpStatus());
    assertEquals(1, body.getItems().size());
    assertEquals(item, body.getItems().get(0));
  }

  @Test
  void createSuccessFailureResponse_withSingleItemAndSuccessFalse_returnsFailureResponse() {
    TestResponse item = new TestResponse(UUID.randomUUID(), "Test Entity");

    ResponseEntity<SuccessFailureResponse<TestResponse>> responseEntity =
        SuccessFailureResponseUtility.createSuccessFailureResponse(
            false, "Record not created", HttpStatus.BAD_REQUEST, item);

    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

    SuccessFailureResponse<TestResponse> body = responseEntity.getBody();

    assertNotNull(body);
    assertEquals(false, body.getSuccess());
    assertEquals("Record not created", body.getMessage());
    assertEquals("Bad Request", body.getHttpStatus());
  }

  @Test
  void createSuccessFailureResponse_withListAndSuccessTrue_returnsSuccessResponse() {
    List<TestResponse> items =
        List.of(
            new TestResponse(UUID.randomUUID(), "First"),
            new TestResponse(UUID.randomUUID(), "Second"));

    ResponseEntity<SuccessFailureResponse<TestResponse>> responseEntity =
        SuccessFailureResponseUtility.createSuccessFailureResponse(
            true, "Records created", HttpStatus.CREATED, items);

    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

    SuccessFailureResponse<TestResponse> body = responseEntity.getBody();

    assertNotNull(body);
    assertEquals(true, body.getSuccess());
    assertEquals("Records created", body.getMessage());
    assertEquals("Created", body.getHttpStatus());
    assertEquals(items, body.getItems());
  }

  @Test
  void createSuccessFailureResponse_withListAndSuccessFalse_returnsFailureResponse() {
    List<TestResponse> items =
        List.of(
            new TestResponse(UUID.randomUUID(), "First"),
            new TestResponse(UUID.randomUUID(), "Second"));

    ResponseEntity<SuccessFailureResponse<TestResponse>> responseEntity =
        SuccessFailureResponseUtility.createSuccessFailureResponse(
            false, "Records not created", HttpStatus.BAD_REQUEST, items);

    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

    SuccessFailureResponse<TestResponse> body = responseEntity.getBody();

    assertNotNull(body);
    assertEquals(false, body.getSuccess());
    assertEquals("Records not created", body.getMessage());
    assertEquals("Bad Request", body.getHttpStatus());
  }

  @Test
  void createSuccessFailureResponse_withoutItemsAndSuccessTrue_returnsSuccessResponse() {
    ResponseEntity<SuccessFailureResponse<TestResponse>> responseEntity =
        SuccessFailureResponseUtility.createSuccessFailureResponse(
            true, "Record deleted", HttpStatus.OK);

    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

    SuccessFailureResponse<TestResponse> body = responseEntity.getBody();

    assertNotNull(body);
    assertEquals(true, body.getSuccess());
    assertEquals("Record deleted", body.getMessage());
    assertEquals("OK", body.getHttpStatus());
  }

  @Test
  void createSuccessFailureResponse_withoutItemsAndSuccessFalse_returnsFailureResponse() {
    ResponseEntity<SuccessFailureResponse<TestResponse>> responseEntity =
        SuccessFailureResponseUtility.createSuccessFailureResponse(
            false, "Record not deleted", HttpStatus.NOT_FOUND);

    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());

    SuccessFailureResponse<TestResponse> body = responseEntity.getBody();

    assertNotNull(body);
    assertEquals(false, body.getSuccess());
    assertEquals("Record not deleted", body.getMessage());
    assertEquals("Not Found", body.getHttpStatus());
  }

  private record TestResponse(UUID id, String name) implements Response {}
}
