UPDATE release
SET s3_file_name = substring(s3_file_name from '[^/]+$');

UPDATE users
SET profile_image = substring(profile_image from '[^/]+$');
