# Personal Finance Analyzer

A local-first finance analytics application for importing bank statements (CSV and PDF), normalizing transactions, categorizing spending, and analyzing monthly credits and debits.

**Current stage:** Phase 1 – repository and application foundation. AWS resources are intentionally deferred until local implementation and testing are complete.

## Planned stack
- Frontend: Angular
- Backend: Java 21, Spring Boot, Maven
- Local database: MySQL
- CSV: Apache Commons CSV
- PDF: Apache PDFBox
- Charts: Apache ECharts (planned)
- Later cloud integration: private Amazon S3

## Planned capabilities
- CSV/PDF statement import
- Transaction extraction and normalization
- Credit/debit identification
- Rule-based transaction categories
- Monthly credits, debits, and net cash-flow summaries
- Searchable transaction history
- Later: private S3 upload, list, and download

## Repository layout
```text
personal-finance-analyzer/
├── backend/
├── frontend/
├── infra/local/
├── docs/
├── sample-data/
├── .gitignore
└── README.md
```

## Privacy and security
- Use synthetic or anonymized statements for development.
- Never commit real bank statements, account numbers, credentials, `.env` files, or private keys.
- Keep future S3 buckets private and use least-privilege IAM access.
- Do not log full transaction descriptions or sensitive account identifiers.

## Phases
1. Repository and application foundation
2. CSV/PDF parsing
3. Transaction normalization and categorization
4. Monthly analytics APIs
5. Angular dashboard
6. Local end-to-end testing
7. S3 integration and upload/download testing
8. Security validation, documentation, and AWS cleanup
9. DevOps portfolio update

This is an educational project, not a banking service or financial-advice product.
