package com.usmarinec.ledger.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

  /** Configuration bean for OpenApi Swagger Doc page. */
  @Bean
  public OpenAPI generalLedgerOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("General Ledger API")
                .description(
                    "Backend API for the General Ledger application. "
                        + "Supports accounting entities, accounts, fiscal years, journal entries, "
                        + "and related ledger operations.")
                .version("v1")
                .contact(new Contact().name("General Ledger Project").email("usmarinec@gmail.com"))
                .license(new License().name("Private / Internal Project")));
  }
}
