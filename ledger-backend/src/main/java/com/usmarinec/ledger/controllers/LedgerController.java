package com.usmarinec.ledger.controllers;

import com.usmarinec.ledger.domain.LedgerDocument;
import com.usmarinec.ledger.dto.CreateRequest;
import com.usmarinec.ledger.dto.Response;
import com.usmarinec.ledger.dto.UpdateRequest;
import com.usmarinec.ledger.repositories.LedgerRepository;
import com.usmarinec.ledger.responses.SuccessFailureResponse;
import com.usmarinec.ledger.responses.SuccessFailureResponseUtility;
import com.usmarinec.ledger.services.LedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
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
    T extends LedgerDocument,
    R extends LedgerRepository<T>,
    CreateReqT extends CreateRequest,
    UpdateReqT extends UpdateRequest,
    ResponseT extends Response,
    S extends LedgerService<T, R, CreateReqT, UpdateReqT, ResponseT>> {
  @Autowired protected S service;

  /**
   * Creates a single record.
   *
   * @param request record type to be created
   * @return SuccessFailureResponse with created record
   */
  @Operation(
      summary = "Create a record",
      description = "Creates one record for the resources handled by this controller.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Record created"),
    @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
    @ApiResponse(
        responseCode = "409",
        description = "Record conflicts with existing resource",
        content = @Content)
  })
  @PostMapping("/create")
  public ResponseEntity<SuccessFailureResponse<ResponseT>> create(
      @Valid @RequestBody CreateReqT request) {
    ResponseT savedDocument = this.service.create(request);
    return SuccessFailureResponseUtility.createSuccessFailureResponse(
        true, "Record created", HttpStatus.CREATED, savedDocument);
  }

  /**
   * Creates a list of records.
   *
   * @param types list of record types
   * @return SuccessFailureResponse with saved records
   */
  @Operation(
      summary = "Create multiple records",
      description = "Creates multiple records for the resource handled by this controller.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Records created"),
    @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
    @ApiResponse(
        responseCode = "409",
        description = "One or more records conflict with existing resources",
        content = @Content)
  })
  @PostMapping("/create-list")
  public ResponseEntity<SuccessFailureResponse<ResponseT>> createList(
      @Valid @RequestBody List<CreateReqT> types) {
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
  @Operation(summary = "Fetch a record by ID", description = "Fetches one record by its UUID.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Record found"),
    @ApiResponse(responseCode = "404", description = "Record not found", content = @Content)
  })
  @GetMapping("/fetch/{id}")
  public ResponseEntity<SuccessFailureResponse<ResponseT>> findById(
      @Parameter(
              description = "UUID of the record to fetch",
              required = true,
              example = "550e8400-e29b-41d4-a716-446655440000")
          @PathVariable
          UUID id) {
    ResponseT foundLedgerDocument = this.service.findById(id);

    return SuccessFailureResponseUtility.createSuccessFailureResponse(
        true, "Record found", HttpStatus.OK, foundLedgerDocument);
  }

  /**
   * Fetch all records.
   *
   * @return SuccessFailureResponse with records
   */
  @Operation(
      summary = "Fetch all records",
      description = "Fetches all records for the resource handled by this controller.")
  @ApiResponse(responseCode = "200", description = "Records retrieved")
  @GetMapping("/fetch")
  public ResponseEntity<SuccessFailureResponse<ResponseT>> getAll() {
    List<ResponseT> types = this.service.findAll();
    return SuccessFailureResponseUtility.createSuccessFailureResponse(
        true, "All records retrieved", HttpStatus.OK, types);
  }

  /**
   * Update record by its id.
   *
   * @param id UUID id value
   * @param request record type to be updated
   * @return SuccessFailureResponse with record
   */
  @Operation(summary = "Update a record", description = "Updates one existing record by its UUID.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Record updated"),
    @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
    @ApiResponse(responseCode = "404", description = "Record not found", content = @Content),
    @ApiResponse(
        responseCode = "409",
        description = "Record conflicts with an existing resource",
        content = @Content)
  })
  @PutMapping("/update/{id}")
  public ResponseEntity<SuccessFailureResponse<ResponseT>> update(
      @Parameter(
              description = "UUID of the record to update",
              required = true,
              example = "550e8400-e29b-41d4-a716-446655440000")
          @PathVariable
          UUID id,
      @Valid @RequestBody UpdateReqT request) {
    ResponseT updatedType = this.service.update(id, request);
    return SuccessFailureResponseUtility.createSuccessFailureResponse(
        true, "Record: '" + id + "' updated", HttpStatus.OK, updatedType);
  }

  /**
   * Delete record by id.
   *
   * @param id string id value
   * @return SuccessFailureResponse with status message
   */
  @Operation(summary = "Delete a record", description = "Deletes one existing record by its UUID.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Record deleted"),
    @ApiResponse(responseCode = "404", description = "Record not found", content = @Content)
  })
  @DeleteMapping("/delete/{id}")
  public ResponseEntity<SuccessFailureResponse<ResponseT>> delete(
      @Parameter(
              description = "UUID of the record to delete",
              required = true,
              example = "550e8400-e29b-41d4-a716-446655440000")
          @PathVariable
          UUID id) {
    this.service.delete(id);
    return SuccessFailureResponseUtility.createSuccessFailureResponse(
        true, "Record deleted with id: '" + id + "'", HttpStatus.OK);
  }

  protected S getService() {
    return this.service;
  }
}
