package com.usmarinec.ledger.controllers.entities;

import com.usmarinec.ledger.controllers.LedgerController;
import com.usmarinec.ledger.domain.entities.AccountingEntity;
import com.usmarinec.ledger.dto.entities.AccountingEntityResponse;
import com.usmarinec.ledger.dto.entities.CreateAccountingEntityRequest;
import com.usmarinec.ledger.dto.entities.UpdateAccountingEntityRequest;
import com.usmarinec.ledger.repositories.entities.AccountingEntityRepository;
import com.usmarinec.ledger.services.entities.AccountingEntityService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AccountingEntityController.API_PATH)
@Tag(
    name = "Accounting Entities",
    description =
        "Manage businesses, organizations, or reporting entities that own accounts, fiscal years, and journal entries.")
public class AccountingEntityController
    extends LedgerController<
        AccountingEntity,
        AccountingEntityRepository,
        CreateAccountingEntityRequest,
        UpdateAccountingEntityRequest,
        AccountingEntityResponse,
        AccountingEntityService> {
  public static final String API_PATH = "/accounting-entities";
}
