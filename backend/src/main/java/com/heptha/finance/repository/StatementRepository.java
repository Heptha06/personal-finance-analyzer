package com.heptha.finance.repository;

import com.heptha.finance.entity.Statement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StatementRepository extends JpaRepository<Statement, Long> {

    List<Statement> findAllByBankAccount_IdOrderByCreatedAtDesc(Long bankAccountId);

    Optional<Statement> findByFileSha256(byte[] fileSha256);
}
