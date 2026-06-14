CREATE TABLE companies (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE fiscal_years (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    year INTEGER NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    closed BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uq_fiscal_year_company_year UNIQUE (company_id, year),
    CONSTRAINT chk_fiscal_year_dates CHECK (start_date <= end_date)
);

CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),

    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,

    account_type VARCHAR(50) NOT NULL,
    normal_balance VARCHAR(10) NOT NULL,
    classification VARCHAR(50) NOT NULL DEFAULT 'NONE',

    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT uq_accounts_company_code UNIQUE (company_id, code),

    CONSTRAINT chk_accounts_account_type CHECK (
        account_type IN (
            'ASSET',
            'LIABILITY',
            'EQUITY',
            'REVENUE',
            'EXPENSE'
        )
    ),

    CONSTRAINT chk_accounts_normal_balance CHECK (
        normal_balance IN ('DEBIT', 'CREDIT')
    ),

    CONSTRAINT chk_accounts_classification CHECK (
        classification IN ('CURRENT', 'NON_CURRENT', 'NONE')
    )
);

CREATE TABLE journal_entries (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    fiscal_year_id UUID NOT NULL REFERENCES fiscal_years(id),

    entry_date DATE NOT NULL,
    entry_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,

    memo TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    posted_at TIMESTAMP,

    CONSTRAINT chk_journal_entry_type CHECK (
        entry_type IN ('STANDARD', 'ADJUSTING', 'CLOSING')
    ),

    CONSTRAINT chk_journal_entry_status CHECK (
        status IN ('DRAFT', 'POSTED', 'VOIDED')
    )
);

