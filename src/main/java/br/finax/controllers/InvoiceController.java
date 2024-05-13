package br.finax.controllers;

import br.finax.dto.InvoiceMonthValues;
import br.finax.dto.InvoiceValues;
import br.finax.models.InvoicePayment;
import br.finax.services.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invoice")
public class InvoiceController {

    public final InvoiceService invoiceService;

    @GetMapping("/get-month-values")
    public InvoiceMonthValues getInvoiceAndReleases(
            @RequestParam long creditCardId, @RequestParam String selectedMonth,
            @RequestParam Date firstDt, @RequestParam Date lastDt
    ) {
        return invoiceService.getInvoiceAndReleases(creditCardId, selectedMonth, firstDt, lastDt);
    }

    @GetMapping("/get-values")
    public InvoiceValues getValues() {
        return invoiceService.getValues();
    }

    @PostMapping("/save-payment")
    public InvoicePayment savePayment(@RequestBody @Valid InvoicePayment payment) {
        return invoiceService.savePayment(payment);
    }

    @DeleteMapping("/delete-payment/{invoicePaymentId}")
    public ResponseEntity<Void> deletePayment(@PathVariable long invoicePaymentId) {
        return invoiceService.deletePayment(invoicePaymentId);
    }

    @PutMapping("/save-payment-attachment/{invoicePaymentId}")
    public InvoicePayment saveInvoiceAttachment(@PathVariable long invoicePaymentId, @RequestParam MultipartFile attachment) {
        return invoiceService.saveInvoiceAttachment(invoicePaymentId, attachment);
    }

    @DeleteMapping("/remove-payment-attachment/{invoicePaymentId}")
    public InvoicePayment removeAttachment(@PathVariable long invoicePaymentId) {
        return invoiceService.removeAttachment(invoicePaymentId);
    }

    @GetMapping("/get-payment-attachment/{invoicePaymentId}")
    public ResponseEntity<byte[]> getPaymentAttachment(@PathVariable long invoicePaymentId) {
        return invoiceService.getPaymentAttachment(invoicePaymentId);
    }
}
