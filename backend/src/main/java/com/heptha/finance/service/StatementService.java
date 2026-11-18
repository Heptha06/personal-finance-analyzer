package com.heptha.finance.service;

import com.heptha.finance.entity.Statement;
import com.heptha.finance.exception.DuplicateResourceException;
import com.heptha.finance.exception.ResourceNotFoundException;
import com.heptha.finance.repository.BankAccountRepository;
import com.heptha.finance.repository.StatementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StatementService {

    private final StatementRepository statementRepository;
    private final BankAccountRepository bankAccountRepository;

    public StatementService(StatementRepository statementRepository,
                            BankAccountRepository bankAccountRepository) {
        this.statementRepository = statementRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    public Statement getById(Long id) {
        return statementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Statement not found: " + id));
    }

    public List<Statement> getByBankAccount(Long bankAccountId) {
        if (!bankAccountRepository.existsById(bankAccountId)) {
            throw new ResourceNotFoundException("Bank account not found: " + bankAccountId);
        }
        return statementRepository.findAllByBankAccount_IdOrderByCreatedAtDesc(bankAccountId);
    }

    public Statement getByChecksum(byte[] fileSha256) {
        validateChecksum(fileSha256);
        return statementRepository.findByFileSha256(fileSha256)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Statement with the supplied checksum was not found."));
    }

    @Transactional
    public Statement register(Statement statement) {
        if (statement == null) {
            throw new IllegalArgumentException("Statement is required.");
        }
        if (statement.getId() != null) {
            throw new IllegalArgumentException("A new statement must not already have an ID.");
        }
        if (statement.getBankAccount() == null || statement.getBankAccount().getId() == null
                || !bankAccountRepository.existsById(statement.getBankAccount().getId())) {
            throw new ResourceNotFoundException("A persisted bank account is required for a statement.");
        }
        byte[] checksum = statement.getFileSha256();
        validateChecksum(checksum);
        if (statementRepository.findByFileSha256(checksum).isPresent()) {
            throw new DuplicateResourceException("A statement with this file checksum already exists.");
        }
        return statementRepository.save(statement);
    }

    private void validateChecksum(byte[] fileSha256) {
        if (fileSha256 == null || fileSha256.length != 32) {
            throw new IllegalArgumentException("Statement SHA-256 checksum must contain 32 bytes.");
        }
    }
}
