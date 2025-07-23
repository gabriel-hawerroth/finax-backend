package br.finax.enums;

import lombok.Getter;

@Getter
public enum S3FolderPath {

    USER_PROFILE_IMG("user/profile-image/"),
    RELEASE_ATTACHMENTS("user/attachments/release/"),
    INVOICE_PAYMENT_ATTACHMENTS("user/attachments/invoice_payment/"),
    DATABASE_BACKUPS("backups/database/");

    private final String path;

    S3FolderPath(String path) {
        this.path = path;
    }
}
