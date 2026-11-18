package com.heptha.finance.repository;

import com.heptha.finance.entity.BankTransaction;
import com.heptha.finance.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {

    List<BankTransaction> findAllByStatement_IdOrderByTransactionDateAscIdAsc(Long statementId);

    List<BankTransaction> findAllByTransactionDateBetweenOrderByTransactionDateAscIdAsc(
            LocalDate fromDate, LocalDate throughDate);

    List<BankTransaction> findAllByTransactionTypeOrderByTransactionDateAscIdAsc(
            TransactionType transactionType);

    List<BankTransaction> findAllByCategoryIdOrderByTransactionDateAscIdAsc(Long categoryId);

    boolean existsByCategoryId(Long categoryId);

    @Query("""
            select t.transactionType as transactionType,
                   sum(t.amount) as signedTotal,
                   sum(abs(t.amount)) as absoluteTotal
            from BankTransaction t
            where t.transactionDate between :fromDate and :throughDate
            group by t.transactionType
            """)
    List<TransactionTypeTotals> aggregateByTypeAndDateRange(
            @Param("fromDate") LocalDate fromDate,
            @Param("throughDate") LocalDate throughDate);
}
