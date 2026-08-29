-- Flyway V2: increase content type code sizes from 4 -> 12
-- Generated: 2026-08-29

SET @@foreign_key_checks = 0;

-- tour_content_types: code, code_multi_lang
ALTER TABLE `tour_content_types` MODIFY COLUMN `code` VARCHAR(16) NOT NULL;
ALTER TABLE `tour_content_types` MODIFY COLUMN `code_multi_lang` VARCHAR(16) NOT NULL;

-- tour_categories: content_type_id, content_type_id_multi_lang
ALTER TABLE `tour_categories` MODIFY COLUMN `content_type_id` VARCHAR(16);
ALTER TABLE `tour_categories` MODIFY COLUMN `content_type_id_multi_lang` VARCHAR(16);
ALTER TABLE `tour_categories` MODIFY COLUMN `code` VARCHAR(16);
ALTER TABLE `tour_categories` MODIFY COLUMN `parent_code` VARCHAR(16);

SET @@foreign_key_checks = 1;

