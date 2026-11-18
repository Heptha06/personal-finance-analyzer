package com.heptha.finance.entity;

import com.heptha.finance.enums.StatementFormat;
import com.heptha.finance.enums.StatementStatus;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "statement",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_statement_storage_key", columnNames = "storage_key"),
                @UniqueConstraint(name = "uq_statement_file_sha256", columnNames = "file_sha256")
        },
        indexes = {
                @Index(name = "idx_statement_account_created", columnList = "bank_account_id, created_at"),
                @Index(name = "idx_statement_status_created", columnList = "status, created_at")
        })
public class Statement extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bank_account_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_statement_bank_account"))
    private BankAccount bankAccount;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "storage_key", nullable = false, length = 512)
    private String storageKey;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "file_sha256", nullable = false, unique = true, length = 32,
            columnDefinition = "BINARY(32)")
    private byte[] fileSha256;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_format", nullable = false, length = 8)
    private StatementFormat sourceFormat;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private StatementStatus status;

    @Column(name = "imported_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime importedAt;

    @OneToMany(mappedBy = "statement", fetch = FetchType.LAZY)
    private List<BankTransaction> transactions = new ArrayList<>();

    protected Statement() {
    }

    public Long getId() {
        return id;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public byte[] getFileSha256() {
        return fileSha256 == null ? null : fileSha256.clone();
    }

    public void setFileSha256(byte[] fileSha256) {
        this.fileSha256 = fileSha256 == null ? null : fileSha256.clone();
    }

    public StatementFormat getSourceFormat() {
        return sourceFormat;
    }

    public void setSourceFormat(StatementFormat sourceFormat) {
        this.sourceFormat = sourceFormat;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public StatementStatus getStatus() {
        return status;
    }

    public void setStatus(StatementStatus status) {
        this.status = status;
    }

    public LocalDateTime getImportedAt() {
        return importedAt;
    }

    public void setImportedAt(LocalDateTime importedAt) {
        this.importedAt = importedAt;
    }

    public List<BankTransaction> getTransactions() {
        return transactions;
    }
}
