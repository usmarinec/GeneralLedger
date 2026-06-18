package com.usmarinec.ledger.services.fiscal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.usmarinec.ledger.domain.entities.AccountingEntity;
import com.usmarinec.ledger.domain.fiscal.FiscalYear;
import com.usmarinec.ledger.dto.fiscal.CreateFiscalYearRequest;
import com.usmarinec.ledger.dto.fiscal.FiscalYearResponse;
import com.usmarinec.ledger.dto.fiscal.UpdateFiscalYearRequest;
import com.usmarinec.ledger.repositories.entities.AccountingEntityRepository;
import com.usmarinec.ledger.repositories.fiscal.FiscalYearRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

class FiscalYearServiceTest {

  private FiscalYearRepository fiscalYearRepository;
  private AccountingEntityRepository accountingEntityRepository;
  private FiscalYearService service;

  @BeforeEach
  void setUp() {
    fiscalYearRepository = mock(FiscalYearRepository.class);
    accountingEntityRepository = mock(AccountingEntityRepository.class);

    service = new FiscalYearService();

    /*
     * Spring is not running in this plain unit test, so @Autowired fields
     * are not populated automatically.
     */
    ReflectionTestUtils.setField(service, "repository", fiscalYearRepository);
    ReflectionTestUtils.setField(service, "accountingEntityRepository", accountingEntityRepository);
  }

  @Test
  void create_whenRequestIsValid_savesFiscalYearAndReturnsResponse() {
    UUID accountingEntityId = UUID.randomUUID();
    final UUID fiscalYearId = UUID.randomUUID();

    AccountingEntity accountingEntity = new AccountingEntity();
    accountingEntity.setId(accountingEntityId);

    CreateFiscalYearRequest request =
        new CreateFiscalYearRequest(
            accountingEntityId, 2026, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

    when(accountingEntityRepository.findById(accountingEntityId))
        .thenReturn(Optional.of(accountingEntity));

    when(fiscalYearRepository.existsByAccountingEntity_IdAndYear(accountingEntityId, 2026))
        .thenReturn(false);

    when(fiscalYearRepository.save(any(FiscalYear.class)))
        .thenAnswer(
            invocation -> {
              FiscalYear fiscalYear = invocation.getArgument(0);
              fiscalYear.setId(fiscalYearId);
              return fiscalYear;
            });

    FiscalYearResponse response = service.create(request);

    assertEquals(fiscalYearId, response.id());
    assertEquals(accountingEntityId, response.accountingEntityId());
    assertEquals(2026, response.year());
    assertEquals(LocalDate.of(2026, 1, 1), response.startDate());
    assertEquals(LocalDate.of(2026, 12, 31), response.endDate());
    assertFalse(response.closed());

    ArgumentCaptor<FiscalYear> captor = ArgumentCaptor.forClass(FiscalYear.class);
    verify(fiscalYearRepository).save(captor.capture());

    FiscalYear savedFiscalYear = captor.getValue();

    assertEquals(accountingEntity, savedFiscalYear.getAccountingEntity());
    assertEquals(2026, savedFiscalYear.getYear());
    assertEquals(LocalDate.of(2026, 1, 1), savedFiscalYear.getStartDate());
    assertEquals(LocalDate.of(2026, 12, 31), savedFiscalYear.getEndDate());
    assertFalse(savedFiscalYear.isClosed());
  }

  @Test
  void create_whenStartDateIsAfterEndDate_throwsBadRequest() {
    UUID accountingEntityId = UUID.randomUUID();

    CreateFiscalYearRequest request =
        new CreateFiscalYearRequest(
            accountingEntityId, 2026, LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1));

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> service.create(request));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

    verifyNoInteractions(accountingEntityRepository);
    verify(fiscalYearRepository, never()).save(any(FiscalYear.class));
  }

  @Test
  void create_whenAccountingEntityDoesNotExist_throwsNotFound() {
    UUID accountingEntityId = UUID.randomUUID();

    CreateFiscalYearRequest request =
        new CreateFiscalYearRequest(
            accountingEntityId, 2026, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

    when(accountingEntityRepository.findById(accountingEntityId)).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> service.create(request));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

    verify(accountingEntityRepository).findById(accountingEntityId);
    verify(fiscalYearRepository, never()).save(any(FiscalYear.class));
  }

  @Test
  void create_whenFiscalYearAlreadyExists_throwsConflict() {
    UUID accountingEntityId = UUID.randomUUID();

    AccountingEntity accountingEntity = new AccountingEntity();
    accountingEntity.setId(accountingEntityId);

    CreateFiscalYearRequest request =
        new CreateFiscalYearRequest(
            accountingEntityId, 2026, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

    when(accountingEntityRepository.findById(accountingEntityId))
        .thenReturn(Optional.of(accountingEntity));

    when(fiscalYearRepository.existsByAccountingEntity_IdAndYear(accountingEntityId, 2026))
        .thenReturn(true);

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> service.create(request));

    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

    verify(fiscalYearRepository, never()).save(any(FiscalYear.class));
  }

  @Test
  void update_whenRequestIsValid_updatesFiscalYearAndReturnsResponse() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID fiscalYearId = UUID.randomUUID();

    AccountingEntity accountingEntity = new AccountingEntity();
    accountingEntity.setId(accountingEntityId);

    FiscalYear existingFiscalYear = new FiscalYear();
    existingFiscalYear.setId(fiscalYearId);
    existingFiscalYear.setAccountingEntity(accountingEntity);
    existingFiscalYear.setYear(2025);
    existingFiscalYear.setStartDate(LocalDate.of(2025, 1, 1));
    existingFiscalYear.setEndDate(LocalDate.of(2025, 12, 31));
    existingFiscalYear.setClosed(false);

    UpdateFiscalYearRequest request =
        new UpdateFiscalYearRequest(
            accountingEntityId, 2026, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), true);

    when(fiscalYearRepository.findById(fiscalYearId)).thenReturn(Optional.of(existingFiscalYear));
    when(accountingEntityRepository.findById(accountingEntityId))
        .thenReturn(Optional.of(accountingEntity));
    when(fiscalYearRepository.findByAccountingEntity_IdAndYear(accountingEntityId, 2026))
        .thenReturn(Optional.empty());
    when(fiscalYearRepository.save(existingFiscalYear)).thenReturn(existingFiscalYear);

    FiscalYearResponse response = service.update(fiscalYearId, request);

    assertEquals(fiscalYearId, response.id());
    assertEquals(accountingEntityId, response.accountingEntityId());
    assertEquals(2026, response.year());
    assertEquals(LocalDate.of(2026, 1, 1), response.startDate());
    assertEquals(LocalDate.of(2026, 12, 31), response.endDate());
    assertEquals(true, response.closed());

    assertEquals(2026, existingFiscalYear.getYear());
    assertEquals(LocalDate.of(2026, 1, 1), existingFiscalYear.getStartDate());
    assertEquals(LocalDate.of(2026, 12, 31), existingFiscalYear.getEndDate());
    assertEquals(true, existingFiscalYear.isClosed());

    verify(fiscalYearRepository).save(existingFiscalYear);
  }

  @Test
  void update_whenDuplicateFiscalYearBelongsToDifferentRecord_throwsConflict() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID fiscalYearId = UUID.randomUUID();
    final UUID otherFiscalYearId = UUID.randomUUID();

    AccountingEntity accountingEntity = new AccountingEntity();
    accountingEntity.setId(accountingEntityId);

    FiscalYear existingFiscalYear = new FiscalYear();
    existingFiscalYear.setId(fiscalYearId);
    existingFiscalYear.setAccountingEntity(accountingEntity);
    existingFiscalYear.setYear(2025);

    FiscalYear duplicateFiscalYear = new FiscalYear();
    duplicateFiscalYear.setId(otherFiscalYearId);
    duplicateFiscalYear.setAccountingEntity(accountingEntity);
    duplicateFiscalYear.setYear(2026);

    UpdateFiscalYearRequest request =
        new UpdateFiscalYearRequest(
            accountingEntityId, 2026, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), false);

    when(fiscalYearRepository.findById(fiscalYearId)).thenReturn(Optional.of(existingFiscalYear));
    when(accountingEntityRepository.findById(accountingEntityId))
        .thenReturn(Optional.of(accountingEntity));
    when(fiscalYearRepository.findByAccountingEntity_IdAndYear(accountingEntityId, 2026))
        .thenReturn(Optional.of(duplicateFiscalYear));

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> service.update(fiscalYearId, request));

    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

    verify(fiscalYearRepository, never()).save(any(FiscalYear.class));
  }

  @Test
  void update_whenMatchingFiscalYearIsSameRecord_allowsUpdate() {
    UUID accountingEntityId = UUID.randomUUID();
    UUID fiscalYearId = UUID.randomUUID();

    AccountingEntity accountingEntity = new AccountingEntity();
    accountingEntity.setId(accountingEntityId);

    FiscalYear existingFiscalYear = new FiscalYear();
    existingFiscalYear.setId(fiscalYearId);
    existingFiscalYear.setAccountingEntity(accountingEntity);
    existingFiscalYear.setYear(2026);
    existingFiscalYear.setStartDate(LocalDate.of(2026, 1, 1));
    existingFiscalYear.setEndDate(LocalDate.of(2026, 12, 31));
    existingFiscalYear.setClosed(false);

    UpdateFiscalYearRequest request =
        new UpdateFiscalYearRequest(
            accountingEntityId, 2026, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), true);

    when(fiscalYearRepository.findById(fiscalYearId)).thenReturn(Optional.of(existingFiscalYear));
    when(accountingEntityRepository.findById(accountingEntityId))
        .thenReturn(Optional.of(accountingEntity));
    when(fiscalYearRepository.findByAccountingEntity_IdAndYear(accountingEntityId, 2026))
        .thenReturn(Optional.of(existingFiscalYear));
    when(fiscalYearRepository.save(existingFiscalYear)).thenReturn(existingFiscalYear);

    FiscalYearResponse response = service.update(fiscalYearId, request);

    assertEquals(fiscalYearId, response.id());
    assertEquals(2026, response.year());
    assertEquals(true, response.closed());

    verify(fiscalYearRepository).save(existingFiscalYear);
  }

  @Test
  void findByAccountingEntity_returnsFiscalYearsForAccountingEntity() {
    UUID accountingEntityId = UUID.randomUUID();

    AccountingEntity accountingEntity = new AccountingEntity();
    accountingEntity.setId(accountingEntityId);

    FiscalYear fiscalYear2026 = new FiscalYear();
    fiscalYear2026.setId(UUID.randomUUID());
    fiscalYear2026.setAccountingEntity(accountingEntity);
    fiscalYear2026.setYear(2026);
    fiscalYear2026.setStartDate(LocalDate.of(2026, 1, 1));
    fiscalYear2026.setEndDate(LocalDate.of(2026, 12, 31));
    fiscalYear2026.setClosed(false);

    FiscalYear fiscalYear2025 = new FiscalYear();
    fiscalYear2025.setId(UUID.randomUUID());
    fiscalYear2025.setAccountingEntity(accountingEntity);
    fiscalYear2025.setYear(2025);
    fiscalYear2025.setStartDate(LocalDate.of(2025, 1, 1));
    fiscalYear2025.setEndDate(LocalDate.of(2025, 12, 31));
    fiscalYear2025.setClosed(true);

    when(fiscalYearRepository.findByAccountingEntity_IdOrderByYearDesc(accountingEntityId))
        .thenReturn(List.of(fiscalYear2026, fiscalYear2025));

    List<FiscalYearResponse> responses = service.findByAccountingEntity(accountingEntityId);

    assertEquals(2, responses.size());

    assertEquals(fiscalYear2026.getId(), responses.get(0).id());
    assertEquals(accountingEntityId, responses.get(0).accountingEntityId());
    assertEquals(2026, responses.get(0).year());
    assertFalse(responses.get(0).closed());

    assertEquals(fiscalYear2025.getId(), responses.get(1).id());
    assertEquals(accountingEntityId, responses.get(1).accountingEntityId());
    assertEquals(2025, responses.get(1).year());
    assertEquals(true, responses.get(1).closed());

    verify(fiscalYearRepository).findByAccountingEntity_IdOrderByYearDesc(accountingEntityId);
  }
}
