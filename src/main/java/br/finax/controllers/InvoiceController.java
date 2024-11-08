package br.finax.controllers;

import br.finax.dto.InvoiceMonthValues;
import br.finax.dto.InvoiceValues;
import br.finax.models.InvoicePayment;
import br.finax.services.InvoicePaymentService;
import br.finax.services.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invoice")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoicePaymentService invoicePaymentService;

    @GetMapping("/get-month-values")
    public ResponseEntity<InvoiceMonthValues> getInvoiceAndReleases(
            @RequestParam long creditCardId, @RequestParam String selectedMonth
    ) {
        return ResponseEntity.ok(
                invoiceService.getMonthValues(creditCardId, selectedMonth)
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
        final InvoicePayment invoicePayment = invoicePaymentService.save(payment);

        if (payment.getId() != null) {
            final URI uri = URI.create("/invoice/" + invoicePayment.getId());

            return ResponseEntity.created(uri).body(invoicePayment);
        }

        return ResponseEntity.ok(invoicePayment);
    }

    @DeleteMapping("/delete-payment/{invoicePaymentId}")
    public ResponseEntity<Void> deletePayment(@PathVariable long invoicePaymentId) {
        invoicePaymentService.deletePayment(invoicePaymentId);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/save-payment-attachment/{invoicePaymentId}")
    public ResponseEntity<InvoicePayment> saveInvoiceAttachment(@PathVariable long invoicePaymentId, @RequestParam MultipartFile attachment) {
        return ResponseEntity.ok(
                invoicePaymentService.savePaymentAttachment(invoicePaymentId, attachment)
        );
    }

    @DeleteMapping("/remove-payment-attachment/{invoicePaymentId}")
    public ResponseEntity<InvoicePayment> removeAttachment(@PathVariable long invoicePaymentId) {
        return ResponseEntity.ok(
                invoicePaymentService.removePaymentAttachment(invoicePaymentId)
        );
    }

    @GetMapping("/get-payment-attachment/{invoicePaymentId}")
    public ResponseEntity<byte[]> getPaymentAttachment(@PathVariable long invoicePaymentId) {
        return ResponseEntity.ok(
                invoicePaymentService.getPaymentAttachment(invoicePaymentId)
        );
    }
}
