package com.usmarinec.ledger.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.usmarinec.ledger.domain.LedgerDocument;
import com.usmarinec.ledger.dto.CreateRequest;
import com.usmarinec.ledger.dto.Response;
import com.usmarinec.ledger.dto.UpdateRequest;
import com.usmarinec.ledger.exceptions.NotFoundException;
import com.usmarinec.ledger.repositories.LedgerRepository;
import com.usmarinec.ledger.responses.SuccessFailureResponse;
import com.usmarinec.ledger.services.LedgerService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class LedgerControllerTest {

  private TestLedgerService service;
  private TestLedgerController controller;

  @BeforeEach
  void setUp() {
    service = mock(TestLedgerService.class);

    controller = new TestLedgerController();

    // Spring is not running in this unit test, so @Autowired will not populate this.
    controller.service = service;
  }

  @Test
  void create_createsRecordAndReturnsCreatedResponse() {
    TestCreateRequest request = new TestCreateRequest("Test Entity");

    TestResponse savedResponse = new TestResponse(UUID.randomUUID(), "Test Entity");

    when(service.create(request)).thenReturn(savedResponse);

    ResponseEntity<SuccessFailureResponse<TestResponse>> responseEntity =
        controller.create(request);

    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

    SuccessFailureResponse<TestResponse> body = responseEntity.getBody();

    assertNotNull(body);
    assertEquals(true, body.getSuccess());
    assertEquals("Record created", body.getMessage());
    assertEquals("Created", body.getHttpStatus());
    assertEquals(1, body.getItems().size());
    assertEquals(savedResponse, body.getItems().get(0));

    verify(service).create(request);
  }

  @Test
  void createList_createsRecordsAndReturnsCreatedResponse() {
    List<TestCreateRequest> requests =
        List.of(new TestCreateRequest("First"), new TestCreateRequest("Second"));

    List<TestResponse> savedResponses =
        List.of(
            new TestResponse(UUID.randomUUID(), "First"),
            new TestResponse(UUID.randomUUID(), "Second"));

    when(service.createList(requests)).thenReturn(savedResponses);

    ResponseEntity<SuccessFailureResponse<TestResponse>> responseEntity =
        controller.createList(requests);

    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

    SuccessFailureResponse<TestResponse> body = responseEntity.getBody();

    assertNotNull(body);
    assertEquals(true, body.getSuccess());
    assertEquals("List of records created", body.getMessage());
    assertEquals("Created", body.getHttpStatus());
    assertEquals(savedResponses, body.getItems());

    verify(service).createList(requests);
  }

  @Test
  void findById_whenRecordExists_returnsOkResponse() {
    UUID id = UUID.randomUUID();

    TestResponse foundResponse = new TestResponse(id, "Test Entity");

    when(service.findById(id)).thenReturn(foundResponse);

    ResponseEntity<SuccessFailureResponse<TestResponse>> responseEntity = controller.findById(id);

    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

    SuccessFailureResponse<TestResponse> body = responseEntity.getBody();

    assertNotNull(body);
    assertEquals(true, body.getSuccess());
    assertEquals("Record found", body.getMessage());
    assertEquals("OK", body.getHttpStatus());
    assertEquals(1, body.getItems().size());
    assertEquals(foundResponse, body.getItems().get(0));

    verify(service).findById(id);
  }

  @Test
  void getAll_returnsOkResponseWithAllRecords() {
    List<TestResponse> foundResponses =
        List.of(
            new TestResponse(UUID.randomUUID(), "First"),
            new TestResponse(UUID.randomUUID(), "Second"));

    when(service.findAll()).thenReturn(foundResponses);

    ResponseEntity<SuccessFailureResponse<TestResponse>> responseEntity = controller.getAll();

    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

    SuccessFailureResponse<TestResponse> body = responseEntity.getBody();

    assertNotNull(body);
    assertEquals(true, body.getSuccess());
    assertEquals("All records retreived", body.getMessage());
    assertEquals("OK", body.getHttpStatus());
    assertEquals(foundResponses, body.getItems());

    verify(service).findAll();
  }

  @Test
  void update_whenRecordExists_updatesRecordAndReturnsOkResponse() {
    UUID id = UUID.randomUUID();

    TestUpdateRequest request = new TestUpdateRequest("Updated Entity");
    TestResponse updatedResponse = new TestResponse(id, "Updated Entity");

    when(service.existsById(id)).thenReturn(true);
    when(service.update(id, request)).thenReturn(updatedResponse);

    ResponseEntity<SuccessFailureResponse<TestResponse>> responseEntity =
        controller.update(id, request);

    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

    SuccessFailureResponse<TestResponse> body = responseEntity.getBody();

    assertNotNull(body);
    assertEquals(true, body.getSuccess());
    assertEquals("Record: '" + id + "' updated", body.getMessage());
    assertEquals("OK", body.getHttpStatus());
    assertEquals(1, body.getItems().size());
    assertEquals(updatedResponse, body.getItems().get(0));

    verify(service).existsById(id);
    verify(service).update(id, request);
  }

  @Test
  void update_whenRecordDoesNotExist_throwsNotFoundException() {
    UUID id = UUID.randomUUID();

    TestUpdateRequest request = new TestUpdateRequest("Updated Entity");

    when(service.existsById(id)).thenReturn(false);

    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> controller.update(id, request));

    assertEquals("Resource with id: '" + id + "' not found", exception.getMessage());

    verify(service).existsById(id);
  }

  @Test
  void delete_whenRecordExists_deletesRecordAndReturnsOkResponse() {
    UUID id = UUID.randomUUID();

    when(service.existsById(id)).thenReturn(true);

    ResponseEntity<SuccessFailureResponse<TestResponse>> responseEntity = controller.delete(id);

    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

    SuccessFailureResponse<TestResponse> body = responseEntity.getBody();

    assertNotNull(body);
    assertEquals(true, body.getSuccess());
    assertEquals("Record deleted with id: '" + id + "'", body.getMessage());
    assertEquals("OK", body.getHttpStatus());

    verify(service).existsById(id);
    verify(service).delete(id);
  }

  @Test
  void delete_whenRecordDoesNotExist_throwsNotFoundException() {
    UUID id = UUID.randomUUID();

    when(service.existsById(id)).thenReturn(false);

    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> controller.delete(id));

    assertEquals("Resource with id: '" + id + "' not found", exception.getMessage());

    verify(service).existsById(id);
  }

  private static class TestDocument extends LedgerDocument {}

  private interface TestRepository extends LedgerRepository<TestDocument> {}

  private record TestCreateRequest(String name) implements CreateRequest {}

  private record TestUpdateRequest(String name) implements UpdateRequest {}

  private record TestResponse(UUID id, String name) implements Response {}

  private abstract static class TestLedgerService
      extends LedgerService<
          TestDocument, TestRepository, TestCreateRequest, TestUpdateRequest, TestResponse> {}

  private static class TestLedgerController
      extends LedgerController<
          TestDocument,
          TestRepository,
          TestCreateRequest,
          TestUpdateRequest,
          TestResponse,
          TestLedgerService> {}
}
