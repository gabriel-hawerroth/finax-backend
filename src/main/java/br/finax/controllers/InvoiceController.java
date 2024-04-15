package br.finax.controllers;

import br.finax.dto.InvoiceMonthValues;
import br.finax.dto.InvoiceValues;
import br.finax.models.InvoicePayment;
import br.finax.services.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invoice")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/get-month-values")
    private InvoiceMonthValues getInvoiceAndReleases(
            @RequestParam long creditCardId, @RequestParam String selectedMonth,
            @RequestParam Date firstDt, @RequestParam Date lastDt
    ) {
        return invoiceService.getInvoiceAndReleases(creditCardId, selectedMonth, firstDt, lastDt);
    }

    @GetMapping("/get-values")
    private InvoiceValues getValues() {
        return invoiceService.getValues();
    }

    @PostMapping("/save-payment")
    private InvoicePayment savePayment(@RequestBody InvoicePayment payment) {
        return invoiceService.savePayment(payment);
    }

    @PutMapping("/save-payment-attachment/{invoicePaymentId}")
    private InvoicePayment saveInvoiceAttachment(@PathVariable long invoicePaymentId, @RequestParam MultipartFile attachment) {
        return invoiceService.saveInvoiceAttachment(invoicePaymentId, attachment);
    }

    @DeleteMapping("/remove-payment-attachment/{invoicePaymentId}")
    private InvoicePayment removeAttachment(@PathVariable long invoicePaymentId) {
        return invoiceService.removeAttachment(invoicePaymentId);
    }
}
