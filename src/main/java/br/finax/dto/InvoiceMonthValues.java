package br.finax.dto;

import br.finax.dto.InterfacesSQL.InvoicePaymentsPerson;
import br.finax.dto.InterfacesSQL.MonthlyReleases;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record InvoiceMonthValues(
        @NotNull
        List<InvoicePaymentsPerson> invoicePayments,

        @NotNull
        List<MonthlyReleases> releases,

        @NotNull
        double previousBalance
) {
}
