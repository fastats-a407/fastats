CREATE DATABASE IF NOT EXISTS fastats;
USE fastats;

DROP TABLE IF EXISTS `sector`;

CREATE TABLE `sector` (
    `id`    INT    NOT NULL AUTO_INCREMENT,
    `code`    VARCHAR(4)    NOT NULL,
    `description`    VARCHAR(16)    NULL,
    CONSTRAINT `PK_SECTOR` PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `stat_survey`;

CREATE TABLE `stat_survey` (
    `id`    INT    NOT NULL AUTO_INCREMENT,
    `sector_id`    INT    NOT NULL,
    `org_code`    INT    NOT NULL,
    `org_name`    VARCHAR(64)    NOT NULL,
    `name`    VARCHAR(64)    NOT NULL,
    CONSTRAINT `PK_STAT_SURVEY` PRIMARY KEY (`id`),
    CONSTRAINT `FK_sector_TO_stat_survey_1` FOREIGN KEY (`sector_id`) REFERENCES `sector` (`id`)
);

DROP TABLE IF EXISTS `stat_table`;

CREATE TABLE `stat_table` (
    `id`    BIGINT    NOT NULL AUTO_INCREMENT,
    `survey_id`    INT    NOT NULL,
    `name`    VARCHAR(255)    NOT NULL,
    `content`    TEXT    NULL,
    `comment`    TEXT    NULL,
    `kosis_tb_id`    VARCHAR(64)    NOT NULL,
    `kosis_view_link`    TEXT    NULL,
    CONSTRAINT `PK_STAT_TABLE` PRIMARY KEY (`id`),
    CONSTRAINT `FK_stat_survey_TO_stat_table_1` FOREIGN KEY (`survey_id`) REFERENCES `stat_survey` (`id`)
);

DROP TABLE IF EXISTS `coll_info`;

CREATE TABLE `coll_info` (
    `id`    BIGINT    NOT NULL AUTO_INCREMENT,
    `stat_table_id`    BIGINT    NOT NULL,
    `start_date`    VARCHAR(8)    NULL,
    `end_date`    VARCHAR(8)    NULL,
    `period`    VARCHAR(4)    NULL,
    CONSTRAINT `PK_COLL_INFO_TABLE` PRIMARY KEY (`id`),
    CONSTRAINT `FK_stat_table_TO_coll_info_1` FOREIGN KEY (`stat_table_id`) REFERENCES `stat_table` (`id`)
);
