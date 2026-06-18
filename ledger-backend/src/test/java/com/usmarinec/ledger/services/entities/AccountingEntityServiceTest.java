package com.usmarinec.ledger.services.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.usmarinec.ledger.domain.entities.AccountingEntity;
import com.usmarinec.ledger.dto.entities.AccountingEntityResponse;
import com.usmarinec.ledger.dto.entities.CreateAccountingEntityRequest;
import com.usmarinec.ledger.dto.entities.UpdateAccountingEntityRequest;
import com.usmarinec.ledger.repositories.entities.AccountingEntityRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class AccountingEntityServiceTest {

  private AccountingEntityRepository repository;
  private AccountingEntityService service;

  @BeforeEach
  void setUp() {
    repository = mock(AccountingEntityRepository.class);
    service = new AccountingEntityService();

    // LedgerService uses @Autowired field injection.
    // Since this is a unit test, Spring is not running.
    ReflectionTestUtils.setField(service, "repository", repository);
  }

  @Test
  void create_mapsCreateRequestToAccountingEntityAndReturnsAccountingEntityResponse() {
    final CreateAccountingEntityRequest request = new CreateAccountingEntityRequest("My Business");

    UUID id = UUID.randomUUID();
    LocalDateTime createdAt = LocalDateTime.of(2026, 6, 17, 12, 0);

    AccountingEntity savedEntity = new AccountingEntity();
    savedEntity.setId(id);
    savedEntity.setName("My Business");
    savedEntity.setCreatedAt(createdAt);

    when(repository.save(any(AccountingEntity.class))).thenReturn(savedEntity);

    AccountingEntityResponse response = service.create(request);

    assertEquals(id, response.id());
    assertEquals("My Business", response.name());
    assertEquals(createdAt, response.createdAt());

    ArgumentCaptor<AccountingEntity> captor = ArgumentCaptor.forClass(AccountingEntity.class);

    verify(repository).save(captor.capture());

    AccountingEntity entityPassedToSave = captor.getValue();

    assertEquals("My Business", entityPassedToSave.getName());
  }

  @Test
  void update_mapsUpdateRequestToExistingAccountingEntityAndReturnsAccountingEntityResponse() {
    UUID id = UUID.randomUUID();
    LocalDateTime createdAt = LocalDateTime.of(2026, 6, 17, 12, 0);

    final UpdateAccountingEntityRequest request =
        new UpdateAccountingEntityRequest("Updated Business");

    AccountingEntity existingEntity = new AccountingEntity();
    existingEntity.setId(id);
    existingEntity.setName("Old Business");
    existingEntity.setCreatedAt(createdAt);

    AccountingEntity savedEntity = new AccountingEntity();
    savedEntity.setId(id);
    savedEntity.setName("Updated Business");
    savedEntity.setCreatedAt(createdAt);

    when(repository.findById(id)).thenReturn(Optional.of(existingEntity));
    when(repository.save(existingEntity)).thenReturn(savedEntity);

    AccountingEntityResponse response = service.update(id, request);

    assertEquals(id, response.id());
    assertEquals("Updated Business", response.name());
    assertEquals(createdAt, response.createdAt());

    assertEquals("Updated Business", existingEntity.getName());

    verify(repository).findById(id);
    verify(repository).save(existingEntity);
  }

  @Test
  void findById_mapsAccountingEntityToAccountingEntityResponse() {
    UUID id = UUID.randomUUID();
    LocalDateTime createdAt = LocalDateTime.of(2026, 6, 17, 12, 0);

    AccountingEntity entity = new AccountingEntity();
    entity.setId(id);
    entity.setName("My Business");
    entity.setCreatedAt(createdAt);

    when(repository.findById(id)).thenReturn(Optional.of(entity));

    AccountingEntityResponse response = service.findById(id);

    assertEquals(id, response.id());
    assertEquals("My Business", response.name());
    assertEquals(createdAt, response.createdAt());

    verify(repository).findById(id);
  }
}
