package br.finax.services;

import br.finax.dto.InterfacesSQL.InvoicePaymentPerson;
import br.finax.enums.ErrorCategory;
import br.finax.enums.S3FolderPath;
import br.finax.exceptions.FileCompressionErrorException;
import br.finax.exceptions.FileIOException;
import br.finax.exceptions.NotFoundException;
import br.finax.exceptions.ServiceException;
import br.finax.exceptions.WithoutPermissionException;
import br.finax.external.AwsS3Service;
import br.finax.models.CreditCard;
import br.finax.models.InvoicePayment;
import br.finax.repository.InvoicePaymentRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

import static br.finax.external.AwsS3Service.getS3FileName;
import static br.finax.utils.FileUtils.*;
import static br.finax.utils.UtilsService.getAuthUser;
import static br.finax.utils.UtilsService.isNotEmpty;

@Service
@RequiredArgsConstructor
public class InvoicePaymentService {

    private final InvoicePaymentRepository invoicePaymentRepository;

    private final CreditCardService creditCardService;
    private final AwsS3Service awsS3Service;

    @Transactional(readOnly = true)
    public InvoicePayment findById(Long id) {
        return invoicePaymentRepository.findById(id)
                .orElseThrow(NotFoundException::new);
    }

    @Transactional
    public InvoicePayment save(InvoicePayment payment) {
        checkPaymentPermission(payment);

        if (payment.getId() != null) {
            final InvoicePayment invoicePayment = findById(payment.getId());

            payment.setS3FileName(invoicePayment.getS3FileName());
            payment.setAttachmentName(invoicePayment.getAttachmentName());
        }

        return invoicePaymentRepository.save(payment);
    }

    @Transactional
    public void deletePayment(long invoicePaymentId) {
        final InvoicePayment payment = findById(invoicePaymentId);

        checkPaymentPermission(payment);

        invoicePaymentRepository.deleteById(invoicePaymentId);
    }

    @Transactional(readOnly = true)
    public List<InvoicePaymentPerson> getInvoicePayments(long userId, long creditCardId, String selectedMonth) {
        if (creditCardService.findUserIdById(creditCardId) != userId)
            throw new WithoutPermissionException();

        return invoicePaymentRepository.getInvoicePayments(creditCardId, selectedMonth);
    }

    @Transactional
    public InvoicePayment savePaymentAttachment(long invoiceId, @NonNull MultipartFile attachment) {
        final InvoicePayment payment = findById(invoiceId);

        checkPaymentPermission(payment);

        final String fileExtension = getFileExtension(attachment);
        final String fileName = getS3FileName(invoiceId, fileExtension);

        try {
            final byte[] compressedFile = compressFile(attachment);

            final File tempFile = convertByteArrayToFile(compressedFile, fileName);

            try {
                if (isNotEmpty(payment.getAttachmentName())) {
                    awsS3Service.updateS3File(
                            concatS3FolderPath(payment.getAttachmentName()),
                            concatS3FolderPath(fileName),
                            tempFile
                    );
                } else {
                    awsS3Service.uploadS3File(
                            concatS3FolderPath(fileName),
                            tempFile
                    );
                }
            } finally {
                var _ = tempFile.delete();
            }

            payment.setS3FileName(fileName);
            payment.setAttachmentName(attachment.getOriginalFilename());

            return save(payment);
        } catch (FileCompressionErrorException | FileIOException | ServiceException e) {
            throw new ServiceException(ErrorCategory.INTERNAL_ERROR, "Failed to process the file", e);
        }
    }

    @Transactional
    public InvoicePayment removePaymentAttachment(long invoiceId) {
        final InvoicePayment payment = findById(invoiceId);

        checkPaymentPermission(payment);

        awsS3Service.deleteS3File(concatS3FolderPath(payment.getS3FileName()));

        payment.setS3FileName(null);
        payment.setAttachmentName(null);

        return invoicePaymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public byte[] getPaymentAttachment(long invoicePaymentId) {
        final InvoicePayment payment = findById(invoicePaymentId);

        checkPaymentPermission(payment);

        return awsS3Service.getS3File(
                concatS3FolderPath(payment.getS3FileName())
        );
    }

    private void checkPaymentPermission(final InvoicePayment payment) {
        final CreditCard card = creditCardService.findById(payment.getCreditCardId());

        if (!card.getUserId().equals(getAuthUser().getId()))
            throw new WithoutPermissionException();
    }

    private String concatS3FolderPath(String filename) {
        return S3FolderPath.INVOICE_PAYMENT_ATTACHMENTS.getPath().concat(filename);
    }
}
