package com.usmarinec.ledger.controllers.entities;

import com.usmarinec.ledger.controllers.LedgerController;
import com.usmarinec.ledger.domain.entities.AccountingEntity;
import com.usmarinec.ledger.dto.entities.AccountingEntityResponse;
import com.usmarinec.ledger.dto.entities.CreateAccountingEntityRequest;
import com.usmarinec.ledger.dto.entities.UpdateAccountingEntityRequest;
import com.usmarinec.ledger.repositories.entities.AccountingEntityRepository;
import com.usmarinec.ledger.services.entities.AccountingEntityService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/AccountingEntity")
public class AccountingEntityController
    extends LedgerController<
        AccountingEntity,
        AccountingEntityRepository,
        CreateAccountingEntityRequest,
        UpdateAccountingEntityRequest,
        AccountingEntityResponse,
        AccountingEntityService> {}
