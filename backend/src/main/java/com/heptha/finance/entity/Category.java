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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "category",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_category_name_type", columnNames = {"name", "transaction_type"}),
                @UniqueConstraint(name = "uq_category_id_type", columnNames = {"id", "transaction_type"})
        })
public class Category extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 16)
    private TransactionType transactionType;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<BankTransaction> transactions = new ArrayList<>();

    protected Category() {
    }

    public Category(String name, TransactionType transactionType) {
        this.name = name;
        this.transactionType = transactionType;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public List<BankTransaction> getTransactions() {
        return transactions;
    }
}
