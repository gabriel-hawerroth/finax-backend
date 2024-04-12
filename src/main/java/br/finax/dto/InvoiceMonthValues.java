package br.finax.dto;

import br.finax.models.InvoicePayment;

import java.util.List;

public record InvoiceMonthValues(
        List<InvoicePayment> invoicePayments,
        List<InterfacesSQL.MonthlyReleases> releases
) {
}
