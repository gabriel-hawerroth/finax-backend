ALTER TABLE public.release
ADD COLUMN created_at timestamptz NOT NULL DEFAULT current_timestamp,
ADD COLUMN updated_at timestamptz NOT NULL DEFAULT current_timestamp;
