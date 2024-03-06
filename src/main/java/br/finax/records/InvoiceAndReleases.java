package br.finax.records;

import br.finax.models.Invoice;
import br.finax.utils.InterfacesSQL;

import java.util.List;

public record InvoiceAndReleases(
        Invoice invoice,
        List<InterfacesSQL.MonthlyReleases> releases
) {}
