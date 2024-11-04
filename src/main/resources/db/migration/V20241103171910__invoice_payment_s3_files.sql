ALTER TABLE invoice_payment
    ADD COLUMN s3_file_name TEXT NULL;

ALTER TABLE invoice_payment
    DROP COLUMN attachment;
