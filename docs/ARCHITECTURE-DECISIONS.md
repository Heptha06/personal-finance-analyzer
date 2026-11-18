# Architecture Decisions

## ADR-001: Local-first
Build and test locally before creating AWS resources. This reduces cloud cost and separates application defects from infrastructure issues.

## ADR-002: Modular monolith
Start with one Spring Boot application divided into statement, transaction, categorization, and analytics packages.

## ADR-003: Source files and normalized data are separate
Retain source documents separately from parsed transaction records. Local uploads remain outside Git.

## ADR-004: Private cloud storage later
Defer S3 until local features and tests pass. Future access must be private and least-privilege.
