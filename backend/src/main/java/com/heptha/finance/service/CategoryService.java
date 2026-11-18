package com.heptha.finance.service;

import com.heptha.finance.entity.Category;
import com.heptha.finance.enums.TransactionType;
import com.heptha.finance.exception.BusinessRuleViolationException;
import com.heptha.finance.exception.DuplicateResourceException;
import com.heptha.finance.exception.ResourceNotFoundException;
import com.heptha.finance.repository.BankTransactionRepository;
import com.heptha.finance.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BankTransactionRepository bankTransactionRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           BankTransactionRepository bankTransactionRepository) {
        this.categoryRepository = categoryRepository;
        this.bankTransactionRepository = bankTransactionRepository;
    }

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public List<Category> getByType(TransactionType transactionType) {
        if (transactionType == null) {
            throw new IllegalArgumentException("Transaction type is required.");
        }
        return categoryRepository.findAllByTransactionTypeOrderByNameAsc(transactionType);
    }

    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

    @Transactional
    public Category create(String name, TransactionType transactionType) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name is required.");
        }
        String normalizedName = name.trim();
        if (normalizedName.length() > 80) {
            throw new IllegalArgumentException("Category name cannot exceed 80 characters.");
        }
        if (transactionType == null) {
            throw new IllegalArgumentException("Category transaction type is required.");
        }
        if (categoryRepository.findByNameAndTransactionType(normalizedName, transactionType).isPresent()) {
            throw new DuplicateResourceException("Category already exists for this transaction type.");
        }
        return categoryRepository.save(new Category(normalizedName, transactionType));
    }

    @Transactional
    public void delete(Long id) {
        Category category = getById(id);
        if (bankTransactionRepository.existsByCategoryId(id)) {
            throw new BusinessRuleViolationException(
                    "Category is assigned to transactions and cannot be deleted.");
        }
        categoryRepository.delete(category);
    }
}
