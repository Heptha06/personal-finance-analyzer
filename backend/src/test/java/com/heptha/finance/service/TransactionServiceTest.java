package com.heptha.finance.service;

import com.heptha.finance.entity.BankTransaction;
import com.heptha.finance.entity.Category;
import com.heptha.finance.enums.TransactionType;
import com.heptha.finance.exception.BusinessRuleViolationException;
import com.heptha.finance.exception.ResourceNotFoundException;
import com.heptha.finance.repository.BankTransactionRepository;
import com.heptha.finance.repository.CategoryRepository;
import com.heptha.finance.repository.StatementRepository;
import com.heptha.finance.repository.TransactionTypeTotals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private BankTransactionRepository bankTransactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private StatementRepository statementRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void dateRangeDelegatesInclusiveBoundariesToRepository() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate through = LocalDate.of(2025, 1, 31);
        when(bankTransactionRepository.findAllByTransactionDateBetweenOrderByTransactionDateAscIdAsc(
                from, through)).thenReturn(List.of());

        assertEquals(List.of(), transactionService.getByDateRange(from, through));
        verify(bankTransactionRepository)
                .findAllByTransactionDateBetweenOrderByTransactionDateAscIdAsc(from, through);
    }

    @Test
    void totalsKeepTransfersSeparateFromIncomeAndExpenses() {
        when(bankTransactionRepository.aggregateByTypeAndDateRange(
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)))
                .thenReturn(List.of(
                        new Aggregate(TransactionType.CREDIT, "125.00", "125.00"),
                        new Aggregate(TransactionType.DEBIT, "-45.25", "45.25"),
                        new Aggregate(TransactionType.TRANSFER, "-20.00", "30.00")));

        TransactionTotals totals = transactionService.getTotals(
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));

        assertEquals(new BigDecimal("125.00"), totals.credits());
        assertEquals(new BigDecimal("45.25"), totals.debits());
        assertEquals(new BigDecimal("30.00"), totals.transfers());
    }

    @Test
    void missingTransactionRaisesNotFound() {
        when(bankTransactionRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.getById(42L));
    }

    @Test
    void assigningWrongCategoryTypeIsRejectedBeforeSave() {
        BankTransaction transaction = org.mockito.Mockito.mock(BankTransaction.class);
        when(bankTransactionRepository.findById(7L)).thenReturn(Optional.of(transaction));
        when(transaction.getTransactionType()).thenReturn(TransactionType.DEBIT);
        when(categoryRepository.findById(9L))
                .thenReturn(Optional.of(new Category("Salary", TransactionType.CREDIT)));

        assertThrows(BusinessRuleViolationException.class,
                () -> transactionService.assignCategory(7L, 9L));
        verify(bankTransactionRepository, never()).save(transaction);
    }

    private record Aggregate(TransactionType transactionType, BigDecimal signedTotal,
                             BigDecimal absoluteTotal) implements TransactionTypeTotals {
        private Aggregate(TransactionType transactionType, String signedTotal, String absoluteTotal) {
            this(transactionType, new BigDecimal(signedTotal), new BigDecimal(absoluteTotal));
        }

        @Override
        public TransactionType getTransactionType() {
            return transactionType;
        }

        @Override
        public BigDecimal getSignedTotal() {
            return signedTotal;
        }

        @Override
        public BigDecimal getAbsoluteTotal() {
            return absoluteTotal;
        }
    }
}
