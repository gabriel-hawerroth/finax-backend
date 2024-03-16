package br.finax.dto;

import br.finax.models.Invoice;
import br.finax.models.InvoicePayment;

import java.util.List;

public record InvoiceMonthValues(
        Invoice invoice,
        List<InvoicePayment> invoicePayments,
        List<InterfacesSQL.MonthlyReleases> releases
) {}
