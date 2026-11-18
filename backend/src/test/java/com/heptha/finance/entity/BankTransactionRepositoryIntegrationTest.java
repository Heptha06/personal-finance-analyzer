package com.heptha.finance.entity;

import com.heptha.finance.enums.StatementFormat;
import com.heptha.finance.enums.StatementStatus;
import com.heptha.finance.enums.TransactionType;
import com.heptha.finance.repository.BankAccountRepository;
import com.heptha.finance.repository.BankTransactionRepository;
import com.heptha.finance.repository.CategoryRepository;
import com.heptha.finance.repository.StatementRepository;
import com.heptha.finance.service.TransactionService;
import com.heptha.finance.service.TransactionTotals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TransactionService.class)
class BankTransactionRepositoryIntegrationTest {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private StatementRepository statementRepository;

    @Autowired
    private BankTransactionRepository bankTransactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionService transactionService;

    private Statement statement;

    @BeforeEach
    void createSyntheticStatement() {
        BankAccount account = bankAccountRepository.saveAndFlush(new BankAccount(
                "Test account", null, com.heptha.finance.enums.AccountType.CHECKING,
                "USD", "****0000"));

        statement = new Statement();
        statement.setBankAccount(account);
        statement.setOriginalFilename("integration-test.csv");
        statement.setStorageKey("test/integration-test.csv");
        statement.setFileSha256(new byte[32]);
        statement.setSourceFormat(StatementFormat.CSV);
        statement.setStatus(StatementStatus.IMPORTED);
        statement = statementRepository.saveAndFlush(statement);
    }

    @Test
    void dateRangeFiltersTransactionsInclusively() {
        saveTransaction(1L, LocalDate.of(2025, 1, 1), "100.00", TransactionType.CREDIT);
        saveTransaction(2L, LocalDate.of(2025, 1, 15), "-20.00", TransactionType.DEBIT);
        saveTransaction(3L, LocalDate.of(2025, 2, 1), "-10.00", TransactionType.TRANSFER);

        List<BankTransaction> found = transactionService.getByDateRange(
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 15));

        assertEquals(2, found.size());
    }

    @Test
    void repositoryFiltersByCategoryAndType() {
        Category food = categoryRepository.findByNameAndTransactionType("Food", TransactionType.DEBIT)
                .orElseThrow();
        BankTransaction debit = saveTransaction(
                1L, LocalDate.of(2025, 1, 10), "-18.25", TransactionType.DEBIT);
        debit.setCategory(food);
        bankTransactionRepository.saveAndFlush(debit);
        saveTransaction(2L, LocalDate.of(2025, 1, 11), "50.00", TransactionType.CREDIT);

        assertEquals(1, transactionService.getByCategory(food.getId()).size());
        assertEquals(1, transactionService.getByType(TransactionType.CREDIT).size());
    }

    @Test
    void totalsAggregateCreditsDebitsAndTransfersSeparately() {
        saveTransaction(1L, LocalDate.of(2025, 1, 2), "125.00", TransactionType.CREDIT);
        saveTransaction(2L, LocalDate.of(2025, 1, 3), "-45.25", TransactionType.DEBIT);
        saveTransaction(3L, LocalDate.of(2025, 1, 4), "-30.00", TransactionType.TRANSFER);

        TransactionTotals totals = transactionService.getTotals(
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));

        assertEquals(new BigDecimal("125.0000"), totals.credits());
        assertEquals(new BigDecimal("45.2500"), totals.debits());
        assertEquals(new BigDecimal("30.0000"), totals.transfers());
    }

    private BankTransaction saveTransaction(Long sourceRow, LocalDate date, String amount,
                                            TransactionType type) {
        BankTransaction transaction = new BankTransaction();
        transaction.setStatement(statement);
        transaction.setSourceRowNumber(sourceRow);
        transaction.setTransactionDate(date);
        transaction.setDescription("Synthetic integration test transaction");
        transaction.setAmount(new BigDecimal(amount));
        transaction.setCurrency("USD");
        transaction.setTransactionType(type);
        return bankTransactionRepository.saveAndFlush(transaction);
    }
}
