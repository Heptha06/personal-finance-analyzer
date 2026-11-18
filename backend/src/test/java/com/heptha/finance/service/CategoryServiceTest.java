package com.heptha.finance.service;

import com.heptha.finance.entity.Category;
import com.heptha.finance.exception.BusinessRuleViolationException;
import com.heptha.finance.repository.BankTransactionRepository;
import com.heptha.finance.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BankTransactionRepository bankTransactionRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void categoryInUseCannotBeDeleted() {
        Category category = new Category("Food", com.heptha.finance.enums.TransactionType.DEBIT);
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(category));
        when(bankTransactionRepository.existsByCategoryId(3L)).thenReturn(true);

        assertThrows(BusinessRuleViolationException.class, () -> categoryService.delete(3L));
        verify(categoryRepository, never()).delete(category);
    }
}
