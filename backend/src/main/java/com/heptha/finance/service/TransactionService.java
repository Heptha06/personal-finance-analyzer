package com.heptha.finance.service;

import com.heptha.finance.entity.BankTransaction;
import com.heptha.finance.enums.TransactionType;
import com.heptha.finance.exception.BusinessRuleViolationException;
import com.heptha.finance.exception.ResourceNotFoundException;
import com.heptha.finance.repository.BankTransactionRepository;
import com.heptha.finance.repository.CategoryRepository;
import com.heptha.finance.repository.StatementRepository;
import com.heptha.finance.repository.TransactionTypeTotals;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TransactionService {

    private final BankTransactionRepository bankTransactionRepository;
    private final CategoryRepository categoryRepository;
    private final StatementRepository statementRepository;

    public TransactionService(BankTransactionRepository bankTransactionRepository,
                              CategoryRepository categoryRepository,
                              StatementRepository statementRepository) {
        this.bankTransactionRepository = bankTransactionRepository;
        this.categoryRepository = categoryRepository;
        this.statementRepository = statementRepository;
    }

    public BankTransaction getById(Long id) {
        return bankTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));
    }

    public List<BankTransaction> getByStatement(Long statementId) {
        if (!statementRepository.existsById(statementId)) {
            throw new ResourceNotFoundException("Statement not found: " + statementId);
        }
        return bankTransactionRepository.findAllByStatement_IdOrderByTransactionDateAscIdAsc(statementId);
    }

    public List<BankTransaction> getByDateRange(LocalDate fromDate, LocalDate throughDate) {
        validateDateRange(fromDate, throughDate);
        return bankTransactionRepository.findAllByTransactionDateBetweenOrderByTransactionDateAscIdAsc(
                fromDate, throughDate);
    }

    public List<BankTransaction> getByType(TransactionType transactionType) {
        if (transactionType == null) {
            throw new IllegalArgumentException("Transaction type is required.");
        }
        return bankTransactionRepository.findAllByTransactionTypeOrderByTransactionDateAscIdAsc(
                transactionType);
    }

    public List<BankTransaction> getByCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found: " + categoryId);
        }
        return bankTransactionRepository.findAllByCategoryIdOrderByTransactionDateAscIdAsc(categoryId);
    }

    public TransactionTotals getTotals(LocalDate fromDate, LocalDate throughDate) {
        validateDateRange(fromDate, throughDate);
        BigDecimal credits = BigDecimal.ZERO;
        BigDecimal debits = BigDecimal.ZERO;
        BigDecimal transfers = BigDecimal.ZERO;

        for (TransactionTypeTotals aggregate : bankTransactionRepository
                .aggregateByTypeAndDateRange(fromDate, throughDate)) {
            switch (aggregate.getTransactionType()) {
                case CREDIT -> credits = aggregate.getSignedTotal();
                case DEBIT -> debits = aggregate.getAbsoluteTotal();
                case TRANSFER -> transfers = aggregate.getAbsoluteTotal();
            }
        }
        return new TransactionTotals(credits, debits, transfers);
    }

    @Transactional
    public BankTransaction assignCategory(Long transactionId, Long categoryId) {
        BankTransaction transaction = getById(transactionId);
        var category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
        if (transaction.getTransactionType() != category.getTransactionType()) {
            throw new BusinessRuleViolationException(
                    "Category transaction type must match the transaction type.");
        }
        transaction.setCategory(category);
        return bankTransactionRepository.save(transaction);
    }

    private void validateDateRange(LocalDate fromDate, LocalDate throughDate) {
        if (fromDate == null || throughDate == null) {
            throw new IllegalArgumentException("Both date range boundaries are required.");
        }
        if (throughDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("End date must not be before start date.");
        }
    }
}
