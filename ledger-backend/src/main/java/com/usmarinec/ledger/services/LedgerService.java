package com.usmarinec.ledger.services;

import com.usmarinec.ledger.domain.LedgerDocument;
import com.usmarinec.ledger.repositories.LedgerRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Base service for common CRUD operations on ledger domain objects.
 *
 * @param <T> the JPA entity type
 * @param <R> the Spring Data repository type
 * @param <CreateRequestT> the create request DTO type
 * @param <UpdateRequestT> the update request DTO type
 * @param <ResponseT> the response DTO type
 */
public abstract class LedgerService<
    T extends LedgerDocument, R extends LedgerRepository<T>, CreateReqT, UpdateReqT, ResponseT> {
  @Autowired protected R repository;

  /**
   * Creates and saves a new ledger document.
   *
   * @param request the create request
   * @return the created document as a response DTO
   */
  @Transactional
  public ResponseT create(CreateReqT request) {
    T ledgerEntity = this.createLedgerEntity(request);
    T savedLedgerEntity = this.repository.save(ledgerEntity);
    return toResponse(savedLedgerEntity);
  }

  /**
   * Finds a ledger document by id.
   *
   * @param id the document id
   * @return the matching document as a response DTO
   */
  @Transactional(readOnly = true)
  public ResponseT findById(UUID id) {
    T ledgerEntity = this.getLedgerEntity(id);
    return this.toResponse(ledgerEntity);
  }

  /**
   * Finds all ledger documents.
   *
   * @return all documents as response DTOs
   */
  @Transactional(readOnly = true)
  public List<ResponseT> findAll() {
    return this.repository.findAll().stream().map(this::toResponse).toList();
  }

  /**
   * Updates an existing ledger document.
   *
   * @param id the document id
   * @param request the update request
   * @return the updated document as a response DTO
   */
  @Transactional
  public ResponseT update(UUID id, UpdateReqT request) {
    T ledgerEntity = this.getLedgerEntity(id);
    this.updateLedgerEntity(ledgerEntity, request);
    T savedLedgerEntity = this.repository.save(ledgerEntity);
    return this.toResponse(savedLedgerEntity);
  }

  /**
   * Deletes a ledger document by id.
   *
   * @param id the document id
   */
  @Transactional
  public void delete(UUID id) {
    T ledgerEntity = this.getLedgerEntity(id);
    this.repository.delete(ledgerEntity);
  }
  
  /**
   * Checks if a record exists in the database.
   * 
   * @param id the document id
   * @return boolean if record found
   */
  @Transactional(readOnly = true)
  public boolean existsById(UUID id){
    return this.repository.existsById(id);
  }

  /**
   * Retrieves a ledger document by id or throws a not-found exception.
   *
   * @param id the document id
   * @return the matching document
   */
  protected T getLedgerEntity(UUID id) {
    return this.repository
        .findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, id.toString() + " Not found"));
  }

  /**
   * Converts a create request into a new ledger document.
   *
   * @param request the create request
   * @return a new unsaved ledger document
   */
  protected abstract T createLedgerEntity(CreateReqT request);

  /**
   * Applies an update request to an existing ledger document.
   *
   * @param ledgerEntity the existing ledger document
   * @param request the update request
   */
  protected abstract void updateLedgerEntity(T ledgerEntity, UpdateReqT request);

  /**
   * Converts a ledger document into a response DTO.
   *
   * @param ledgerEntity the ledger document
   * @return the response DTO
   */
  protected abstract ResponseT toResponse(T ledgerEntity);
}
