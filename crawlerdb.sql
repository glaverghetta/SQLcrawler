-- MySQL Script generated by MySQL Workbench
-- Thu Jan 12 00:00:03 2023
-- Model: New Model    Version: 1.0
-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema crawler
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema crawler
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `crawler` DEFAULT CHARACTER SET utf8 ;
USE `crawler` ;

-- -----------------------------------------------------
-- Table `crawler`.`projects`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `crawler`.`projects` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `gh_id` VARCHAR(50) CHARACTER SET 'utf8' COLLATE 'utf8_bin' NULL DEFAULT NULL,
  `owner` VARCHAR(50) NULL DEFAULT NULL,
  `name` VARCHAR(110) NULL DEFAULT NULL,
  `url` TEXT NULL DEFAULT NULL,
  `source` VARCHAR(45) NULL DEFAULT NULL,
  `date_added` DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `gh_id_UNIQUE` (`gh_id` ASC) INVISIBLE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `crawler`.`files`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `crawler`.`files` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project` INT NOT NULL,
  `filename` VARCHAR(200) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_0900_as_cs' NULL DEFAULT NULL,
  `lang` VARCHAR(45) NULL DEFAULT NULL,
  `path` TEXT CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_0900_as_cs' NULL DEFAULT NULL,
  `url` TEXT NULL DEFAULT NULL,
  `hash` VARCHAR(45) NULL DEFAULT NULL,
  `fileSize` INT NULL DEFAULT NULL,
  `date_added` DATETIME NULL DEFAULT NULL,
  `commit_date` DATETIME NULL DEFAULT NULL,
  `commit` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`id`, `project`),
  INDEX `fk_files_projects_idx` (`project` ASC) VISIBLE,
  CONSTRAINT `fk_files_projects`
    FOREIGN KEY (`project`)
    REFERENCES `crawler`.`projects` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `crawler`.`analyses`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `crawler`.`analyses` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project` INT NOT NULL,
  `file` INT NOT NULL,
  `analysis_date` DATETIME NULL DEFAULT NULL,
  `sql_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT') NULL DEFAULT NULL,
  `is_parameterized` TINYINT(1) NULL DEFAULT NULL,
  `api_type` VARCHAR(45) NULL DEFAULT NULL,
  `sql_usage_lower` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY') NULL DEFAULT NULL,
  `order_group_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY', 'STRING_CONCAT_LIST') NULL DEFAULT NULL,
  `like_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY') NULL DEFAULT NULL,
  `column_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY', 'STRING_CONCAT_LIST') NULL DEFAULT NULL,
  `table_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY', 'STRING_CONCAT_LIST') NULL DEFAULT NULL,
  `table_usage_lower` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY') NULL DEFAULT NULL,
  `view_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY') NULL DEFAULT NULL,
  `proc_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY') NULL DEFAULT NULL,
  `fun_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY') NULL DEFAULT NULL,
  `event_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY') NULL DEFAULT NULL,
  `trig_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY') NULL DEFAULT NULL,
  `index_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY') NULL DEFAULT NULL,
  `db_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY') NULL DEFAULT NULL,
  `server_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY') NULL DEFAULT NULL,
  `tspace_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY') NULL DEFAULT NULL,
  PRIMARY KEY (`id`, `project`, `file`),
  INDEX `fk_analyses_projects1_idx` (`project` ASC) VISIBLE,
  INDEX `fk_analyses_files1_idx` (`file` ASC) VISIBLE,
  CONSTRAINT `fk_analyses_files1`
    FOREIGN KEY (`file`)
    REFERENCES `crawler`.`files` (`id`),
  CONSTRAINT `fk_analyses_projects1`
    FOREIGN KEY (`project`)
    REFERENCES `crawler`.`projects` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `crawler`.`repo_info`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `crawler`.`repo_info` (
  `project` INT NOT NULL,
  `gh_id` VARCHAR(100) NULL DEFAULT NULL,
  `description` TEXT CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_0900_ai_ci' NULL DEFAULT NULL,
  `releasesCount` INT NULL DEFAULT NULL,
  `LRName` VARCHAR(200) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_0900_ai_ci' NULL DEFAULT NULL,
  `LRCreated` DATETIME NULL DEFAULT NULL,
  `LRUpdated` DATETIME NULL DEFAULT NULL,
  `stargazerCount` INT NULL DEFAULT NULL,
  `forkCount` INT NULL DEFAULT NULL,
  `watchersCount` INT NULL DEFAULT NULL,
  `createdAt` DATETIME NULL DEFAULT NULL,
  `updatedAt` DATETIME NULL DEFAULT NULL,
  `pushedAt` DATETIME NULL DEFAULT NULL,
  `date_added` DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (`project`),
  INDEX `fk_repo_projects1_idx` (`project` ASC) VISIBLE,
  CONSTRAINT `fk_repo_projects1`
    FOREIGN KEY (`project`)
    REFERENCES `crawler`.`projects` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
