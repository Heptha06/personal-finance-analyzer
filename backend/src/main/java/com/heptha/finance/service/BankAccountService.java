package com.heptha.finance.service;

import com.heptha.finance.entity.BankAccount;
import com.heptha.finance.exception.BusinessRuleViolationException;
import com.heptha.finance.exception.ResourceNotFoundException;
import com.heptha.finance.repository.BankAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;

    public BankAccountService(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    public BankAccount getById(Long id) {
        return bankAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found: " + id));
    }

    public BankAccount getSingleAccount() {
        var accounts = bankAccountRepository.findAll();
        if (accounts.isEmpty()) {
            throw new ResourceNotFoundException("No bank account has been configured.");
        }
        if (accounts.size() > 1) {
            throw new BusinessRuleViolationException(
                    "Version 1 supports exactly one bank account.");
        }
        return accounts.getFirst();
    }

    @Transactional
    public BankAccount create(BankAccount bankAccount) {
        if (bankAccount == null) {
            throw new IllegalArgumentException("Bank account is required.");
        }
        if (bankAccountRepository.count() > 0) {
            throw new BusinessRuleViolationException(
                    "Version 1 supports only one bank account; existing data was not changed.");
        }
        return bankAccountRepository.save(bankAccount);
    }
}
