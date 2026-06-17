package com.usmarinec.ledger.controllers;

import com.usmarinec.ledger.domain.LedgerDocument;
import com.usmarinec.ledger.repositories.LedgerRepository;
import com.usmarinec.ledger.services.LedgerService;
import com.usmarinec.ledger.responses.SuccessFailureResponse;
import com.usmarinec.ledger.responses.SuccessFailureResponseUtility;
import com.usmarinec.ledger.exceptions.NotFoundException;
import com.usmarinec.ledger.dto.Response;
import com.usmarinec.ledger.dto.CreateRequest;
import com.usmarinec.ledger.dto.UpdateRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

public abstract class LedgerController<
    T extends LedgerDocument, R extends LedgerRepository<T>, CreateReqT extends CreateRequest, UpdateReqT extends UpdateRequest, ResponseT extends Response, S extends LedgerService<T, R, CreateReqT, UpdateReqT, ResponseT>> {
  @Autowired S service;
  
  /**
   * Creates a single record.
   *
   * @param ledgerDocument record type to be created
   * @return SuccessFailureResponse with created record
   */
  @PostMapping("/create")
  public ResponseEntity<SuccessFailureResponse<ResponseT>> create(@RequestBody CreateReqT request) {
      ResponseT savedDocument = this.service.create(request);
      return SuccessFailureResponseUtility.createSuccessFailureResponse(true, "Record created", HttpStatus.CREATED, savedDocument);
  }
  
    /**
   * Creates a list of records.
   *
   * @param types list of record types
   * @return SuccessFailureResponse with saved records
   */
  @PostMapping("/create-list")
  public ResponseEntity<SuccessFailureResponse<ResponseT>> createList(@RequestBody List<CreateReqT> types) {
    List<ResponseT> savedTypes = this.service.createList(types);
    return SuccessFailureResponseUtility.createSuccessFailureResponse(
        true, "List of records created", HttpStatus.CREATED, savedTypes);
  }

  /**
   * Fetch record by its id.
   *
   * @param id string id value
   * @return SuccessFailureResponse with record
   */
  @GetMapping("/fetch/{id}")
  public ResponseEntity<SuccessFailureResponse<ResponseT>> findById(@PathVariable UUID id) {
    ResponseT foundLedgerDocument = this.service.findById(id);

    return SuccessFailureResponseUtility.createSuccessFailureResponse(true, "Record found", HttpStatus.OK, foundLedgerDocument);
  }
  /**
   * Fetch all records.
   *
   * @return SuccessFailureResponse with records
   */
  @GetMapping("/fetch")
  public ResponseEntity<SuccessFailureResponse<ResponseT>> getAll() {
    List<ResponseT> types = this.service.findAll();
    return SuccessFailureResponseUtility.createSuccessFailureResponse(
        true, "All records retreived", HttpStatus.OK, types);
  }


  /**
   * Update record by its id.
   *
   * @param id string id value
   * @param type record type to be updated
   * @return SuccessFailureResponse with record
   */
  @PutMapping("/update/{id}")
  public ResponseEntity<SuccessFailureResponse<ResponseT>> update(
      @PathVariable UUID id, @RequestBody UpdateReqT request) {
    if (this.service.existsById(id)) {
      ResponseT updatedType = this.service.update(id, request);
      return SuccessFailureResponseUtility.createSuccessFailureResponse(
          true, "Record: '" + id + "' updated", HttpStatus.OK, updatedType);
    } else {
      throw new NotFoundException("Resource with id: '" + id + "' not found");
    }
  }

  /**
   * Delete record by id.
   *
   * @param id string id value
   * @return SuccessFailureResponse with status message
   */
  @DeleteMapping("/delete/{id}")
  public ResponseEntity<SuccessFailureResponse<ResponseT>> delete(@PathVariable UUID id) {
    if (this.service.existsById(id)) {
      this.service.delete(id);
      return SuccessFailureResponseUtility.createSuccessFailureResponse(
          true, "Record deleted with id: '" + id + "'", HttpStatus.OK);
    } else {
      throw new NotFoundException("Resource with id: '" + id + "' not found");
    }
  }

  protected S getService() {
    return this.service;
  }
}
