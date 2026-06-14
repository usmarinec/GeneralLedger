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

