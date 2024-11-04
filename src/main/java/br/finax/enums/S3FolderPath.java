package br.finax.enums;

import lombok.Getter;

@Getter
public enum S3FolderPath {

    IMGS("imgs/"),
    BANK_IMGS("imgs/banks/"),
    USER_PROFILE_IMG("user/profile-image/"),
    RELEASE_ATTACHMENTS("user/attachments/release/"),
    INVOICE_PAYMENT_ATTACHMENTS("user/attachments/invoice_payment/");

    private final String path;

    S3FolderPath(String path) {
        this.path = path;
    }
}
