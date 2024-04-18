package br.finax.dto;

import br.finax.dto.InterfacesSQL.InvoicePaymentsPerson;
import br.finax.dto.InterfacesSQL.MonthlyReleases;

import java.util.List;

public record InvoiceMonthValues(
        List<InvoicePaymentsPerson> invoicePayments,
        List<MonthlyReleases> releases,
        double previousBalance
) {
}
