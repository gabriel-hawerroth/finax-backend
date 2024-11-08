package br.finax.dto;

import br.finax.dto.InterfacesSQL.InvoicePaymentPerson;
import br.finax.dto.InterfacesSQL.MonthlyRelease;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record InvoiceMonthValues(
        @NotNull
        List<InvoicePaymentPerson> invoicePayments,

        @NotNull
        List<MonthlyRelease> releases,

        @NotNull
        BigDecimal previousBalance
) {
}
