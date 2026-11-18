# Phase 1 – Validation and Troubleshooting Report

**Checked:** 2026-11-18

## Safety

- No AWS resources or AWS credentials were created or configured.
- No real financial statements were used.
- No npm audit fixes, Git commits, or pushes were made.

## Environment

| Tool | Result |
| --- | --- |
| Java | OpenJDK 21.0.8 at `C:\Program Files\Java\jdk-21` |
| Maven | 3.9.12; ran with process-local `JAVA_HOME` and an ignored local repository under `backend/target/m2repo` |
| Node.js / npm | Node v20.19.5 / npm 10.8.2 |
| Angular CLI | 20.3.1 |
| Docker | Not available on PATH |
| MySQL | `127.0.0.1:3306` refused the connection; no database service was started |

## Completed

- Verified all nine Angular packages in `package.json` and `package-lock.json` are exactly **20.3.1**: common, compiler, core, forms, platform-browser, router, build, CLI, and compiler-cli. `npm ls` also reports each installed package at 20.3.1.
- `npm ci --dry-run` completed successfully and did not replace `node_modules`.
- `npm run build` passed with Angular 20.3.1. Output: `frontend/dist/finance-analyzer`.
- `npm audit` completed without applying fixes. It reported 47 vulnerable dependency entries: 1 low, 20 moderate, 24 high, 2 critical.
- Static Compose review confirms port binding is only `127.0.0.1:3306`; database name and user match backend defaults. `DB_PASSWORD` is required by the app and passed to MySQL as `MYSQL_PASSWORD`; `MYSQL_ROOT_PASSWORD` is required for the healthcheck.
- Updated backend startup to fail before Spring initialization if `DB_PASSWORD` is missing. `mvn spring-boot:run` with no `DB_PASSWORD` exited with: `Required environment variable DB_PASSWORD is not set. Set it to the local MySQL password before starting the backend.`
- Maven compiled application and test sources. `git diff --check` had no whitespace errors.

## Security audit detail

**Direct high severity dependencies (12):** `@angular/build`, `@angular/cli`, `@angular/common`, `@angular/compiler`, `@angular/compiler-cli`, `@angular/core`, `@angular/forms`, `@angular/platform-browser`, `@angular/router`, `karma`, `karma-jasmine`, and `karma-jasmine-html-reporter`.

**Transitive high severity dependencies (12):** `@modelcontextprotocol/sdk`, `@parcel/watcher`, `anymatch`, `chokidar`, `node-gyp`, `pacote`, `picomatch`, `readdirp`, `sigstore`, `tinyglobby`, `vite`, and `zod`.

**Transitive critical dependencies (2):** `piscina` (prototype-pollution gadget with possible RCE) and `tar` (archive path traversal/arbitrary file read-write issues).

**Other transitive findings:** 20 moderate (`@angular-devkit/architect`, `@angular-devkit/core`, `@angular-devkit/schematics`, `@npmcli/package-json`, `@npmcli/run-script`, `@schematics/angular`, `@sigstore/core`, `@sigstore/sign`, `@sigstore/tuf`, `@sigstore/verify`, `@tufjs/models`, `ajv`, `cacache`, `glob`, `ignore-walk`, `make-fetch-happen`, `minimatch`, `npm-packlist`, `npm-registry-fetch`, `tuf-js`) and 1 low (`@babel/core`).

Angular advisories include XSS sanitization issues, XSRF token leakage, transfer-cache information leaks, and SSR router denial of service. npm reports non-major Angular patch fixes: framework/compiler packages to 20.3.35 and CLI/build to 20.3.43. Those fixes change the exact 20.3.1 pins, so none were applied. The three direct Karma packages have fixes marked major by npm's solver; review the test runner separately rather than accepting an automatic rewrite.

**Recommended plan:** if approved to move off exact 20.3.1, update Angular packages together to a fixed patch set within the Angular 20.3 line, review the Karma test-runner path, then rerun clean install, build, tests, and audit. Do not use `npm audit fix --force`.

## Failed

- `mvn test` ran one test and reported one error because MySQL was unavailable. Hibernate saw a communications link failure and could not determine its dialect. The test did not pass.
- Earlier npm install attempts recorded Windows `EPERM` errors while creating and cleaning folders under `frontend/node_modules`. Current ACL inspection shows the account has full control and the current dependency tree is complete. The historical cause cannot be established from those logs alone.
- Docker Compose syntax and healthcheck could not be executed because Docker is unavailable.

## Pending

- A real `npm ci` was not run because it replaces the existing `node_modules`; run it manually only when that replacement is acceptable. The dry-run and exact-version build passed.
- Compose validation, MySQL health, backend startup with valid MySQL credentials, and a passing Maven test run remain pending until Docker/MySQL is available.

## Files changed during validation

- `backend/src/main/resources/application.yml`: now requires `${DB_PASSWORD}` with no empty fallback.
- `backend/src/main/java/com/heptha/finance/FinanceAnalyzerApplication.java`: checks `DB_PASSWORD` before Spring starts and prints a clear error if missing.
- `infra/local/docker-compose.yml`: uses required `DB_PASSWORD` and `MYSQL_ROOT_PASSWORD` environment variables, with no default credentials. The loopback-only binding remains.
- This report.

## Manual commands

After confirming it is acceptable for `npm ci` to replace the current `node_modules`, run from the project’s `frontend/` directory (prefer a normal PowerShell terminal if the Codex sandbox repeats `EPERM`):

```powershell
npm cache verify
npm ci
npm run build
```

After Docker is installed, choose local-only values in the shell; use the same app password for MySQL and Spring:

```powershell
$env:DB_PASSWORD = '<local-only app password>'
$env:MYSQL_ROOT_PASSWORD = '<local-only MySQL root password>'
docker compose -f infra/local/docker-compose.yml config --quiet
docker compose -f infra/local/docker-compose.yml up -d
docker compose -f infra/local/docker-compose.yml ps
```

Then run the backend tests with Java 21 and the writable ignored Maven cache:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
cd backend
$repoCache = Join-Path (Get-Location) 'target\m2repo'
mvn "-Dmaven.repo.local=$repoCache" test
```
