package br.finax;

import br.finax.enums.S3FolderPath;
import br.finax.external.AwsS3Service;
import br.finax.models.Category;
import br.finax.repository.CategoryRepository;
import br.finax.repository.InvoicePaymentRepository;
import br.finax.repository.ReleaseRepository;
import br.finax.repository.UserRepository;
import br.finax.security.SecurityFilter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class Schedule {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ReleaseRepository releaseRepository;
    private final InvoicePaymentRepository invoicePaymentRepository;

    private final SecurityFilter securityFilter;
    private final AwsS3Service s3Service;

    @PersistenceContext
    private final EntityManager entityManager;

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    // Method for not leaving the machine idle
    @Scheduled(cron = "45 * * * * *") //every minute
    public void stayActive() {
        List<Category> categories = categoryRepository.findAll();

        for (var i = 0; i < categories.size(); i++) {
            categories.remove(categories.get(i));
        }
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void clearUsersCache() { //every day at 3:40AM
        securityFilter.clearUsersCache();

        log.info("Cleared user cache in security filter");
    }

    @Transactional
    @Scheduled(cron = "0 05 3 * * *") //every day at 3:30AM
    public void optimizeDatabase() {
        final String dbName = databaseUrl.split("//")[1].split("/")[1].split("\\?")[0];

        entityManager.createNativeQuery("commit; vacuum full analyze; commit;").executeUpdate();
        entityManager.createNativeQuery("commit; reindex database " + dbName + "; commit;").executeUpdate();

        log.info("Database optimized");
    }

    @Transactional
    @Scheduled(cron = "0 0 4 1 * *")
    public void checkS3UnusedProfileImages() {
        final List<String> dbProfileImages = userRepository.getAllUserProfileImages();

        checkS3UnusedObjects(S3FolderPath.USER_PROFILE_IMG, dbProfileImages);
    }

    @Scheduled(cron = "0 05 4 1 * *")
    public void checkS3UnusedReleaseAttachments() {
        final List<String> dbAttachments = releaseRepository.getAllReleaseAttachments();

        checkS3UnusedObjects(S3FolderPath.RELEASE_ATTACHMENTS, dbAttachments);
    }

    @Scheduled(cron = "0 10 4 1 * *")
    public void checkS3UnusedInvoicePaymentAttachments() {
        final List<String> dbAttachments = invoicePaymentRepository.getAllInvoicePaymentAttachments();

        checkS3UnusedObjects(S3FolderPath.INVOICE_PAYMENT_ATTACHMENTS, dbAttachments);
    }

    private void checkS3UnusedObjects(S3FolderPath folderPath, List<String> dbObjects) {
        final List<S3Object> s3Objects = s3Service.getAllObjectsInPath(folderPath);

        final var objectsToDelete = new LinkedList<S3Object>();

        s3Objects.forEach(obj -> {
            final String fileName = obj.key().replace(folderPath.getPath(), "");

            if (!dbObjects.contains(fileName))
                objectsToDelete.add(obj);
        });

        if (!objectsToDelete.isEmpty()) {
            s3Service.deleteS3Files(objectsToDelete);
            log.info("Deleted {} unused objects in S3 - path: {}", objectsToDelete.size(), folderPath.getPath());
        }
    }
}
