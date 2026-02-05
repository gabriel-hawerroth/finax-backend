CREATE INDEX release_user_id_date_idx ON "release" USING btree (user_id, date);
DROP INDEX IF EXISTS release_user_id_idx;
DROP INDEX IF EXISTS release_done_idx;