package com.usmarinec.ledger.config;

import com.usmarinec.ledger.dto.openapi.OpenApiResponses;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiResponseSchemaConfig {

  private static final String APPLICATION_JSON = "application/json";

  /** Assigns correct response types to OpenApi Swagger doc. */
  @Bean
  public OpenApiCustomizer ledgerResponseSchemaCustomizer() {
    return openApi -> {
      registerSchema(openApi, OpenApiResponses.AccountSuccessFailureResponse.class);
      registerSchema(openApi, OpenApiResponses.AccountingEntitySuccessFailureResponse.class);
      registerSchema(openApi, OpenApiResponses.FiscalYearSuccessFailureResponse.class);
      registerSchema(openApi, OpenApiResponses.JournalEntrySuccessFailureResponse.class);
      registerSchema(openApi, OpenApiResponses.TrialBalanceSuccessFailureResponse.class);
      registerSchema(openApi, OpenApiResponses.MessageSuccessFailureResponse.class);

      openApi
          .getPaths()
          .forEach(
              (path, pathItem) -> {
                if (isPathForResource(path, "/accounting-entities")) {
                  applyResponseSchema(pathItem, "AccountingEntitySuccessFailureResponse");
                } else if (isPathForResource(path, "/accounts")) {
                  applyResponseSchema(pathItem, "AccountSuccessFailureResponse");
                } else if (isPathForResource(path, "/fiscal-years")) {
                  applyResponseSchema(pathItem, "FiscalYearSuccessFailureResponse");
                } else if (isPathForResource(path, "/journal-entries")) {
                  applyResponseSchema(pathItem, "JournalEntrySuccessFailureResponse");
                } else if (isPathForResource(path, "/trial-balances")) {
                  applyResponseSchema(pathItem, "TrialBalanceSuccessFailureResponse");
                }
              });
    };
  }

  private boolean isPathForResource(String path, String resourcePath) {
    return path.equals(resourcePath)
        || path.startsWith(resourcePath + "/")
        || path.equals("/api" + resourcePath)
        || path.startsWith("/api" + resourcePath + "/");
  }

  private void applyResponseSchema(PathItem pathItem, String schemaName) {
    applyResponseSchema(pathItem.getGet(), "200", schemaName);
    applyResponseSchema(pathItem.getPost(), "200", schemaName);
    applyResponseSchema(pathItem.getPost(), "201", schemaName);
    applyResponseSchema(pathItem.getPut(), "200", schemaName);

    /*
     * Delete endpoints return the standard wrapper but usually do not return
     * resource DTO items.
     */
    applyResponseSchema(pathItem.getDelete(), "200", "MessageSuccessFailureResponse");
  }

  private void applyResponseSchema(Operation operation, String responseCode, String schemaName) {
    if (operation == null || operation.getResponses() == null) {
      return;
    }

    var apiResponse = operation.getResponses().get(responseCode);

    if (apiResponse == null) {
      return;
    }

    Schema<?> schema = new Schema<>().$ref("#/components/schemas/" + schemaName);

    apiResponse.content(
        new Content().addMediaType(APPLICATION_JSON, new MediaType().schema(schema)));
  }

  private void registerSchema(OpenAPI openApi, Class<?> schemaClass) {
    if (openApi.getComponents() == null) {
      openApi.setComponents(new Components());
    }

    ResolvedSchema resolvedSchema =
        ModelConverters.getInstance()
            .resolveAsResolvedSchema(new AnnotatedType(schemaClass).resolveAsRef(false));

    if (resolvedSchema.referencedSchemas != null) {
      resolvedSchema.referencedSchemas.forEach(openApi.getComponents()::addSchemas);
    }

    if (resolvedSchema.schema != null) {
      String schemaName = resolvedSchema.schema.getName();

      if (schemaName == null || schemaName.isBlank()) {
        schemaName = schemaClass.getSimpleName();
      }

      openApi.getComponents().addSchemas(schemaName, resolvedSchema.schema);
    }
  }
}
