package com.heptha.finance.entity;

import com.heptha.finance.enums.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "bank_transaction",
        uniqueConstraints = @UniqueConstraint(name = "uq_transaction_statement_source_row",
                columnNames = {"statement_id", "source_row_number"}),
        indexes = {
                @Index(name = "idx_transaction_statement_date", columnList = "statement_id, transaction_date"),
                @Index(name = "idx_transaction_date_id", columnList = "transaction_date, id"),
                @Index(name = "idx_transaction_type_date", columnList = "transaction_type, transaction_date"),
                @Index(name = "idx_transaction_category_type", columnList = "category_id, transaction_type")
        })
public class BankTransaction extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "statement_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_transaction_statement"))
    private Statement statement;

    @Column(name = "source_row_number", nullable = false, columnDefinition = "INT UNSIGNED")
    private Long sourceRowNumber;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "posted_date")
    private LocalDate postedDate;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "merchant", length = 255)
    private String merchant;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3, columnDefinition = "CHAR(3)")
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 16)
    private TransactionType transactionType;

    @Column(name = "running_balance", precision = 19, scale = 4)
    private BigDecimal runningBalance;

    /** Writable scalar mapping for the nullable category_id column. */
    @Column(name = "category_id")
    private Long categoryId;

    /**
     * Both join columns are read-only here. categoryId and transactionType own the
     * writable column mappings, while this association retains the composite FK join.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(value = {
            @JoinColumn(name = "category_id", referencedColumnName = "id",
                    insertable = false, updatable = false),
            @JoinColumn(name = "transaction_type", referencedColumnName = "transaction_type",
                    insertable = false, updatable = false)
    }, foreignKey = @ForeignKey(name = "fk_transaction_category_type"))
    private Category category;

    protected BankTransaction() {
    }

    public Long getId() {
        return id;
    }

    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    public Long getSourceRowNumber() {
        return sourceRowNumber;
    }

    public void setSourceRowNumber(Long sourceRowNumber) {
        this.sourceRowNumber = sourceRowNumber;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public LocalDate getPostedDate() {
        return postedDate;
    }

    public void setPostedDate(LocalDate postedDate) {
        this.postedDate = postedDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        if (category != null && category.getTransactionType() != transactionType) {
            throw new IllegalArgumentException("Category type must match transaction type.");
        }
        this.transactionType = transactionType;
    }

    public BigDecimal getRunningBalance() {
        return runningBalance;
    }

    public void setRunningBalance(BigDecimal runningBalance) {
        this.runningBalance = runningBalance;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        if (category != null && !Objects.equals(category.getId(), categoryId)) {
            category = null;
        }
        this.categoryId = categoryId;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        if (category != null && transactionType != null
                && category.getTransactionType() != transactionType) {
            throw new IllegalArgumentException("Category type must match transaction type.");
        }
        this.category = category;
        this.categoryId = category == null ? null : category.getId();
    }
}
