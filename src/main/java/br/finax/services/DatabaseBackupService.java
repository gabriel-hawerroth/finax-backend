package br.finax.services;

import br.finax.enums.S3FolderPath;
import br.finax.external.AwsS3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseBackupService {

    private final AwsS3Service awsS3Service;

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUsername;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    public void performBackup() {
        File backupFile = null;
        try {
            log.info("Starting database backup...");

            // Extrair informações da URL do banco
            final String[] urlParts = databaseUrl.replace("jdbc:postgresql://", "").split("/");
            final String hostAndPort = urlParts[0];
            final String dbNameWithParams = urlParts[1];
            final String dbName = dbNameWithParams.split("\\?")[0];
            final String host = hostAndPort.split(":")[0];
            final String port = hostAndPort.contains(":") ? hostAndPort.split(":")[1] : "5432";

            log.info("Database connection details - Host: {}, Port: {}, Database: {}, User: {}",
                    host, port, dbName, databaseUsername);

            // Gerar nome do arquivo de backup com timestamp
            final String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            final String backupFileName = String.format("finax_backup_%s.sql", timestamp);

            // Criar arquivo temporário para o backup
            backupFile = File.createTempFile("finax_backup_", ".sql");

            // Executar pg_dump
            final ProcessBuilder processBuilder = new ProcessBuilder(
                    "pg_dump",
                    "-h", host,
                    "-p", port,
                    "-U", databaseUsername,
                    "-d", dbName,
                    "-f", backupFile.getAbsolutePath(),
                    "--verbose",
                    "--no-password"
            );

            // Configurar variáveis de ambiente para autenticação
            processBuilder.environment().put("PGPASSWORD", databasePassword);

            // Redirecionar output para capturar erros
            processBuilder.redirectErrorStream(true);

            log.info("Executing pg_dump command...");
            final Process process = processBuilder.start();

            // Capturar output do processo
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("pg_dump output: {}", line);
                }
            }

            final int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("pg_dump completed successfully. Backup file size: {} bytes", backupFile.length());

                // Upload do backup para S3
                final String s3Key = S3FolderPath.DATABASE_BACKUPS.getPath() + backupFileName;
                awsS3Service.uploadS3File(s3Key, backupFile);

                log.info("Database backup completed successfully. File uploaded to S3: {}", s3Key);

                // Limpar backups antigos (manter apenas os últimos 10 dias)
                awsS3Service.cleanOldBackups();
            } else {
                log.error("Database backup failed with exit code: {}", exitCode);

                // Se o arquivo foi criado mas o processo falhou, vamos verificar seu conteúdo
                if (backupFile.exists() && backupFile.length() > 0) {
                    log.info("Backup file was created with size: {} bytes, but process failed", backupFile.length());
                }
            }

        } catch (IOException | InterruptedException e) {
            log.error("Error during database backup", e);
        } catch (Exception e) {
            log.error("Unexpected error during database backup", e);
        } finally {
            // Limpar arquivo temporário
            if (backupFile != null && backupFile.exists()) {
                try {
                    Files.delete(backupFile.toPath());
                    log.debug("Temporary backup file deleted: {}", backupFile.getAbsolutePath());
                } catch (IOException e) {
                    log.warn("Failed to delete temporary backup file: {}", backupFile.getAbsolutePath(), e);
                }
            }
        }
    }
}
