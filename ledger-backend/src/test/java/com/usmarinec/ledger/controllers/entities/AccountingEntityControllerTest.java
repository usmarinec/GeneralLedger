package com.usmarinec.ledger.controllers.entities;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

class AccountingEntityControllerTest {

  @Test
  void accountingEntityController_hasRestControllerAnnotation() {
    RestController restController =
        AccountingEntityController.class.getAnnotation(RestController.class);

    assertNotNull(restController);
  }

  @Test
  void accountingEntityController_hasExpectedBaseRequestMapping() {
    RequestMapping requestMapping =
        AccountingEntityController.class.getAnnotation(RequestMapping.class);

    assertNotNull(requestMapping);
    assertArrayEquals(new String[] {"/accounting-entities"}, requestMapping.value());
  }
}
