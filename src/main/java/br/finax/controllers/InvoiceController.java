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

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invoice")
public class InvoiceController {

    public final InvoiceService invoiceService;

    @GetMapping("/get-month-values")
    public ResponseEntity<InvoiceMonthValues> getInvoiceAndReleases(
            @RequestParam long creditCardId, @RequestParam String selectedMonth
    ) {
        return ResponseEntity.ok(
                invoiceService.getInvoiceAndReleases(creditCardId, selectedMonth)
        );
    }

    @GetMapping("/get-values")
    public ResponseEntity<InvoiceValues> getValues() {
        return ResponseEntity.ok(
                invoiceService.getValues()
        );
    }

    @PostMapping("/save-payment")
    public ResponseEntity<InvoicePayment> savePayment(@RequestBody @Valid InvoicePayment payment) {
        final InvoicePayment invoicePayment = invoiceService.savePayment(payment);

        if (payment.getId() != null) {
            final URI uri = URI.create("/invoice/" + invoicePayment.getId());

            return ResponseEntity.created(uri).body(invoicePayment);
        }

        return ResponseEntity.ok(invoicePayment);
    }

    @DeleteMapping("/delete-payment/{invoicePaymentId}")
    public ResponseEntity<Void> deletePayment(@PathVariable long invoicePaymentId) {
        invoiceService.deletePayment(invoicePaymentId);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/save-payment-attachment/{invoicePaymentId}")
    public ResponseEntity<InvoicePayment> saveInvoiceAttachment(@PathVariable long invoicePaymentId, @RequestParam MultipartFile attachment) {
        return ResponseEntity.ok(
                invoiceService.savePaymentAttachment(invoicePaymentId, attachment)
        );
    }

    @DeleteMapping("/remove-payment-attachment/{invoicePaymentId}")
    public ResponseEntity<InvoicePayment> removeAttachment(@PathVariable long invoicePaymentId) {
        return ResponseEntity.ok(
                invoiceService.removePaymentAttachment(invoicePaymentId)
        );
    }

    @GetMapping("/get-payment-attachment/{invoicePaymentId}")
    public ResponseEntity<byte[]> getPaymentAttachment(@PathVariable long invoicePaymentId) {
        return ResponseEntity.ok(
                invoiceService.getPaymentAttachment(invoicePaymentId)
        );
    }
}
