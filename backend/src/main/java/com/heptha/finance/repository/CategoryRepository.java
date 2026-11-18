package com.heptha.finance.repository;

import com.heptha.finance.entity.Category;
import com.heptha.finance.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByTransactionTypeOrderByNameAsc(TransactionType transactionType);

    Optional<Category> findByNameAndTransactionType(String name, TransactionType transactionType);
}
