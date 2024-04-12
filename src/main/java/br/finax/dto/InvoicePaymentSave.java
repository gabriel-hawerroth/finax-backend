package br.finax.dto;

import br.finax.models.InvoicePayment;
import org.springframework.web.multipart.MultipartFile;

public record InvoicePaymentSave(
        InvoicePayment payment,
        MultipartFile attachment
) {
}
