CREATE TABLE bank_account (
    id BIGINT NOT NULL AUTO_INCREMENT,
    display_name VARCHAR(100) NOT NULL,
    institution_name VARCHAR(150) NULL,
    account_type VARCHAR(24) NOT NULL,
    currency CHAR(3) NOT NULL,
    masked_identifier VARCHAR(32) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_bank_account PRIMARY KEY (id),
    CONSTRAINT chk_bank_account_currency CHECK (CHAR_LENGTH(currency) = 3)
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE statement (
    id BIGINT NOT NULL AUTO_INCREMENT,
    bank_account_id BIGINT NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    storage_key VARCHAR(512) NOT NULL,
    file_sha256 BINARY(32) NOT NULL,
    source_format VARCHAR(8) NOT NULL,
    period_start DATE NULL,
    period_end DATE NULL,
    status VARCHAR(16) NOT NULL,
    imported_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_statement PRIMARY KEY (id),
    CONSTRAINT uq_statement_storage_key UNIQUE (storage_key),
    CONSTRAINT uq_statement_file_sha256 UNIQUE (file_sha256),
    CONSTRAINT fk_statement_bank_account FOREIGN KEY (bank_account_id)
        REFERENCES bank_account (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT chk_statement_source_format CHECK (source_format IN ('CSV', 'PDF')),
    CONSTRAINT chk_statement_status CHECK (
        status IN ('RECEIVED', 'PROCESSING', 'IMPORTED', 'FAILED')
    ),
    CONSTRAINT chk_statement_period CHECK (
        period_start IS NULL OR period_end IS NULL OR period_end >= period_start
    ),
    INDEX idx_statement_account_created (bank_account_id, created_at),
    INDEX idx_statement_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE category (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL,
    transaction_type VARCHAR(16) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_category PRIMARY KEY (id),
    CONSTRAINT uq_category_name_type UNIQUE (name, transaction_type),
    CONSTRAINT uq_category_id_type UNIQUE (id, transaction_type),
    CONSTRAINT chk_category_transaction_type CHECK (
        transaction_type IN ('CREDIT', 'DEBIT', 'TRANSFER')
    )
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE bank_transaction (
    id BIGINT NOT NULL AUTO_INCREMENT,
    statement_id BIGINT NOT NULL,
    source_row_number INT UNSIGNED NOT NULL,
    transaction_date DATE NOT NULL,
    posted_date DATE NULL,
    description VARCHAR(500) NOT NULL,
    merchant VARCHAR(255) NULL,
    amount DECIMAL(19,4) NOT NULL,
    currency CHAR(3) NOT NULL,
    transaction_type VARCHAR(16) NOT NULL,
    running_balance DECIMAL(19,4) NULL,
    category_id BIGINT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_bank_transaction PRIMARY KEY (id),
    CONSTRAINT uq_transaction_statement_source_row UNIQUE (statement_id, source_row_number),
    CONSTRAINT fk_transaction_statement FOREIGN KEY (statement_id)
        REFERENCES statement (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_transaction_category_type FOREIGN KEY (category_id, transaction_type)
        REFERENCES category (id, transaction_type)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT chk_transaction_type CHECK (
        transaction_type IN ('CREDIT', 'DEBIT', 'TRANSFER')
    ),
    CONSTRAINT chk_transaction_amount_sign CHECK (
        (transaction_type = 'CREDIT' AND amount > 0)
        OR (transaction_type = 'DEBIT' AND amount < 0)
        OR (transaction_type = 'TRANSFER' AND amount <> 0)
    ),
    CONSTRAINT chk_transaction_source_row CHECK (source_row_number > 0),
    CONSTRAINT chk_transaction_currency CHECK (CHAR_LENGTH(currency) = 3),
    INDEX idx_transaction_statement_date (statement_id, transaction_date),
    INDEX idx_transaction_date_id (transaction_date, id),
    INDEX idx_transaction_type_date (transaction_type, transaction_date),
    INDEX idx_transaction_category_type (category_id, transaction_type)
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO category (name, transaction_type) VALUES
    ('Salary', 'CREDIT'),
    ('Interest', 'CREDIT'),
    ('Refund', 'CREDIT'),
    ('Other Income', 'CREDIT'),
    ('Food', 'DEBIT'),
    ('Shopping', 'DEBIT'),
    ('Bills', 'DEBIT'),
    ('Travel', 'DEBIT'),
    ('Healthcare', 'DEBIT'),
    ('Other Expense', 'DEBIT'),
    ('Self Transfer', 'TRANSFER'),
    ('Bank Transfer', 'TRANSFER'),
    ('Other Transfer', 'TRANSFER');
