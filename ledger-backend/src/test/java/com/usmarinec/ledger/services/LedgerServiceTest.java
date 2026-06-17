package com.usmarinec.ledger.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.usmarinec.ledger.domain.LedgerDocument;
import com.usmarinec.ledger.dto.CreateRequest;
import com.usmarinec.ledger.dto.Response;
import com.usmarinec.ledger.dto.UpdateRequest;
import com.usmarinec.ledger.repositories.LedgerRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class LedgerServiceTest {

  private TestRepository repository;
  private TestLedgerService service;

  @BeforeEach
  void setUp() {
    repository = mock(TestRepository.class);

    service = new TestLedgerService();

    // Because this is a plain unit test, Spring is not running.
    // Therefore @Autowired will not fill this field for us.
    service.repository = repository;
  }

  @Test
  void create_savesEntityAndReturnsResponse() {
    final TestCreateRequest request = new TestCreateRequest("Test Entity");

    TestDocument savedDocument = new TestDocument();
    savedDocument.setId(UUID.randomUUID());
    savedDocument.setName("Test Entity");

    when(repository.save(any(TestDocument.class))).thenReturn(savedDocument);

    TestResponse response = service.create(request);

    assertEquals(savedDocument.getId(), response.id());
    assertEquals("Test Entity", response.name());

    verify(repository).save(any(TestDocument.class));
  }

  @Test
  void createList_savesAllEntitiesAndReturnsAllResponses() {
    List<TestCreateRequest> requests =
        List.of(new TestCreateRequest("First"), new TestCreateRequest("Second"));

    TestDocument savedFirst = new TestDocument();
    savedFirst.setId(UUID.randomUUID());
    savedFirst.setName("First");

    TestDocument savedSecond = new TestDocument();
    savedSecond.setId(UUID.randomUUID());
    savedSecond.setName("Second");

    when(repository.saveAll(any())).thenReturn(List.of(savedFirst, savedSecond));

    List<TestResponse> responses = service.createList(requests);

    assertEquals(2, responses.size());

    assertEquals(savedFirst.getId(), responses.get(0).id());
    assertEquals("First", responses.get(0).name());

    assertEquals(savedSecond.getId(), responses.get(1).id());
    assertEquals("Second", responses.get(1).name());

    verify(repository).saveAll(any());
  }

  @Test
  void createList_whenRequestListIsEmpty_returnsEmptyList() {
    when(repository.saveAll(any())).thenReturn(List.of());

    List<TestResponse> responses = service.createList(List.of());

    assertEquals(0, responses.size());

    verify(repository).saveAll(any());
  }

  @Test
  void findById_whenEntityExists_returnsResponse() {
    UUID id = UUID.randomUUID();

    TestDocument document = new TestDocument();
    document.setId(id);
    document.setName("Test Entity");

    when(repository.findById(id)).thenReturn(Optional.of(document));

    TestResponse response = service.findById(id);

    assertEquals(id, response.id());
    assertEquals("Test Entity", response.name());
  }

  @Test
  void findById_whenEntityDoesNotExist_throwsNotFoundException() {
    UUID id = UUID.randomUUID();

    when(repository.findById(id)).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> service.findById(id));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void findAll_returnsAllResponses() {
    TestDocument first = new TestDocument();
    first.setId(UUID.randomUUID());
    first.setName("First");

    TestDocument second = new TestDocument();
    second.setId(UUID.randomUUID());
    second.setName("Second");

    when(repository.findAll()).thenReturn(List.of(first, second));

    List<TestResponse> responses = service.findAll();

    assertEquals(2, responses.size());

    assertEquals(first.getId(), responses.get(0).id());
    assertEquals("First", responses.get(0).name());

    assertEquals(second.getId(), responses.get(1).id());
    assertEquals("Second", responses.get(1).name());
  }

  @Test
  void update_whenEntityExists_updatesEntitySavesItAndReturnsResponse() {
    UUID id = UUID.randomUUID();

    TestDocument existingDocument = new TestDocument();
    existingDocument.setId(id);
    existingDocument.setName("Old Name");

    TestDocument savedDocument = new TestDocument();
    savedDocument.setId(id);
    savedDocument.setName("New Name");

    when(repository.findById(id)).thenReturn(Optional.of(existingDocument));
    when(repository.save(existingDocument)).thenReturn(savedDocument);

    TestResponse response = service.update(id, new TestUpdateRequest("New Name"));

    assertEquals(id, response.id());
    assertEquals("New Name", response.name());

    assertEquals("New Name", existingDocument.getName());

    verify(repository).save(existingDocument);
  }

  @Test
  void update_whenEntityDoesNotExist_throwsNotFoundException() {
    UUID id = UUID.randomUUID();

    when(repository.findById(id)).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> service.update(id, new TestUpdateRequest("New Name")));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void delete_whenEntityExists_deletesEntity() {
    UUID id = UUID.randomUUID();

    TestDocument document = new TestDocument();
    document.setId(id);
    document.setName("Test Entity");

    when(repository.findById(id)).thenReturn(Optional.of(document));

    service.delete(id);

    verify(repository).delete(document);
  }

  @Test
  void delete_whenEntityDoesNotExist_throwsNotFoundException() {
    UUID id = UUID.randomUUID();

    when(repository.findById(id)).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> service.delete(id));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void existsById_whenEntityExists_returnsTrue() {
    UUID id = UUID.randomUUID();

    when(repository.existsById(id)).thenReturn(true);

    boolean exists = service.existsById(id);

    assertTrue(exists);

    verify(repository).existsById(id);
  }

  @Test
  void existsById_whenEntityDoesNotExist_returnsFalse() {
    UUID id = UUID.randomUUID();

    when(repository.existsById(id)).thenReturn(false);

    boolean exists = service.existsById(id);

    assertFalse(exists);

    verify(repository).existsById(id);
  }

  private static class TestDocument extends LedgerDocument {
    private String name;

    String getName() {
      return name;
    }

    void setName(String name) {
      this.name = name;
    }
  }

  private interface TestRepository extends LedgerRepository<TestDocument> {}

  private record TestCreateRequest(String name) implements CreateRequest {}

  private record TestUpdateRequest(String name) implements UpdateRequest {}

  private record TestResponse(UUID id, String name) implements Response {}

  private static class TestLedgerService
      extends LedgerService<
          TestDocument, TestRepository, TestCreateRequest, TestUpdateRequest, TestResponse> {

    @Override
    protected TestDocument createLedgerEntity(TestCreateRequest request) {
      TestDocument document = new TestDocument();
      document.setName(request.name());
      return document;
    }

    @Override
    protected void updateLedgerEntity(TestDocument ledgerEntity, TestUpdateRequest request) {
      ledgerEntity.setName(request.name());
    }

    @Override
    protected TestResponse toResponse(TestDocument ledgerEntity) {
      return new TestResponse(ledgerEntity.getId(), ledgerEntity.getName());
    }
  }
}
