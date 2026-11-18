package com.heptha.finance.repository;

import com.heptha.finance.enums.TransactionType;

import java.math.BigDecimal;

public interface TransactionTypeTotals {

    TransactionType getTransactionType();

    BigDecimal getSignedTotal();

    BigDecimal getAbsoluteTotal();
}
