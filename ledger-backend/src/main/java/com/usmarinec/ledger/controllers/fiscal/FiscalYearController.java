package com.usmarinec.ledger.controllers.fiscal;

import com.usmarinec.ledger.controllers.LedgerController;
import com.usmarinec.ledger.domain.fiscal.FiscalYear;
import com.usmarinec.ledger.dto.fiscal.CreateFiscalYearRequest;
import com.usmarinec.ledger.dto.fiscal.FiscalYearResponse;
import com.usmarinec.ledger.dto.fiscal.UpdateFiscalYearRequest;
import com.usmarinec.ledger.repositories.fiscal.FiscalYearRepository;
import com.usmarinec.ledger.services.fiscal.FiscalYearService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/FiscalYear")
public class FiscalYearController
    extends LedgerController<
        FiscalYear,
        FiscalYearRepository,
        CreateFiscalYearRequest,
        UpdateFiscalYearRequest,
        FiscalYearResponse,
        FiscalYearService> {}
