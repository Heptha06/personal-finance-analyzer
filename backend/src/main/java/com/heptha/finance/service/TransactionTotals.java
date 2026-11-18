package com.heptha.finance.service;

import java.math.BigDecimal;

/** Aggregated money totals; debit expenses and transfer volume are returned as positive values. */
public record TransactionTotals(BigDecimal credits, BigDecimal debits, BigDecimal transfers) {

    public TransactionTotals {
        credits = credits == null ? BigDecimal.ZERO : credits;
        debits = debits == null ? BigDecimal.ZERO : debits;
        transfers = transfers == null ? BigDecimal.ZERO : transfers;
    }
}
