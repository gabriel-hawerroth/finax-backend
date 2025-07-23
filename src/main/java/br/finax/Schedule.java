package br.finax;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.finax.enums.S3FolderPath;
import br.finax.external.AwsS3Service;
import br.finax.models.Category;
import br.finax.repository.CategoryRepository;
import br.finax.repository.InvoicePaymentRepository;
import br.finax.repository.ReleaseRepository;
import br.finax.repository.UserRepository;
import br.finax.security.SecurityFilter;
import br.finax.services.DatabaseBackupService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class Schedule {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ReleaseRepository releaseRepository;
    private final InvoicePaymentRepository invoicePaymentRepository;

    private final SecurityFilter securityFilter;
    private final AwsS3Service awsS3Service;
    private final DatabaseBackupService databaseBackupService;

    @PersistenceContext
    private final EntityManager entityManager;

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    // Method for not leaving the machine idle
    @Scheduled(cron = "45 * * * * *")
    public void stayActive() {
        List<Category> categories = categoryRepository.findAll();

        for (var i = 0; i < categories.size(); i++) {
            categories.remove(categories.get(i));
        }
    }

    @Scheduled(cron = "0 30 1 * * *")
    public void clearUsersCache() {
        securityFilter.clearUsersCache();
        log.info("Cleared user cache in security filter");
    }

    @Transactional
    @Scheduled(cron = "0 0 2 * * *")
    public void optimizeDatabase() {
        final String dbName = databaseUrl.split("//")[1].split("/")[1].split("\\?")[0];

        entityManager.createNativeQuery("commit; vacuum full analyze; commit;").executeUpdate();
        entityManager.createNativeQuery("commit; reindex database " + dbName + "; commit;").executeUpdate();

        log.info("Database optimized");
    }

    @Scheduled(cron = "0 30 2 * * *")
    public void backupDatabase() {
        databaseBackupService.performBackup();
    }

    @Transactional
    @Scheduled(cron = "0 0 3 1 * *")
    public void checkS3UnusedProfileImages() {
        final List<String> dbProfileImages = userRepository.getAllUserProfileImages();
        awsS3Service.checkUnusedObjects(S3FolderPath.USER_PROFILE_IMG, dbProfileImages);
    }

    @Scheduled(cron = "0 05 3 1 * *")
    public void checkS3UnusedReleaseAttachments() {
        final List<String> dbAttachments = releaseRepository.getAllReleaseAttachments();
        awsS3Service.checkUnusedObjects(S3FolderPath.RELEASE_ATTACHMENTS, dbAttachments);
    }

    @Scheduled(cron = "0 10 3 1 * *")
    public void checkS3UnusedInvoicePaymentAttachments() {
        final List<String> dbAttachments = invoicePaymentRepository.getAllInvoicePaymentAttachments();
        awsS3Service.checkUnusedObjects(S3FolderPath.INVOICE_PAYMENT_ATTACHMENTS, dbAttachments);
    }
}
