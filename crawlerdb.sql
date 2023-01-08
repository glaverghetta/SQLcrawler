-- MySQL Workbench Forward Engineering


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
  `name` VARCHAR(200) NULL,
  `url` TEXT NULL,
  `source` VARCHAR(45) NULL,
  `date_added` DATETIME NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `crawler`.`files`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `crawler`.`files` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project` INT NOT NULL,
  `filename` VARCHAR(200) NULL,
  `lang` VARCHAR(45) NULL,
  `path` TEXT NULL,
  `url` TEXT NULL,
  `hash` VARCHAR(45) NULL,
  `date_added` DATETIME NULL,
  `commit_date` DATETIME NULL,
  `commit` VARCHAR(45) NULL,
  PRIMARY KEY (`id`, `project`),
  INDEX `fk_files_projects_idx` (`project` ASC) VISIBLE,
  CONSTRAINT `fk_files_projects`
    FOREIGN KEY (`project`)
    REFERENCES `crawler`.`projects` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `crawler`.`repo_info`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `crawler`.`repo_info` (
  `project` INT NOT NULL,
  `fork_count` INT NULL,
  `watch_count` INT NULL,
  `star_count` INT NULL,
  `last_commit_date` DATETIME NULL,
  `forked_from` TEXT NULL,
  `total_commit` INT NULL,
  `total_branch` INT NULL,
  `total_release` INT NULL,
  `total_contr` INT NULL,
  PRIMARY KEY (`project`),
  INDEX `fk_repo_projects1_idx` (`project` ASC) VISIBLE,
  CONSTRAINT `fk_repo_projects1`
    FOREIGN KEY (`project`)
    REFERENCES `crawler`.`projects` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `crawler`.`analyses`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `crawler`.`analyses` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project` INT NOT NULL,
  `file` INT NOT NULL,
  `analysis_date` DATETIME NULL,
  `sql_usage` ENUM("NONE", "HARDCODED", "STRING_CONCAT") NULL,
  `is_parameterized` BOOLEAN,
  `api_type` VARCHAR(45) NULL,
  `sql_usage_lower` ENUM("NONE", "HARDCODED", "STRING_CONCAT", "PARAMATIZED_QUERY_AND_CONCAT", "PARAMATIZED_QUERY") NULL,
  `order_group_usage` ENUM("NONE", "HARDCODED", "STRING_CONCAT", "PARAMATIZED_QUERY_AND_CONCAT", "PARAMATIZED_QUERY", "STRING_CONCAT_LIST") NULL,
  `like_usage` ENUM("NONE", "HARDCODED", "STRING_CONCAT", "PARAMATIZED_QUERY_AND_CONCAT", "PARAMATIZED_QUERY") NULL,
  `column_usage` ENUM("NONE", "HARDCODED", "STRING_CONCAT", "PARAMATIZED_QUERY_AND_CONCAT", "PARAMATIZED_QUERY", "STRING_CONCAT_LIST") NULL,
  `table_usage` ENUM("NONE", "HARDCODED", "STRING_CONCAT", "PARAMATIZED_QUERY_AND_CONCAT", "PARAMATIZED_QUERY", "STRING_CONCAT_LIST") NULL,
  `table_usage_lower` ENUM("NONE", "HARDCODED", "STRING_CONCAT", "PARAMATIZED_QUERY_AND_CONCAT", "PARAMATIZED_QUERY") NULL,
  `view_usage` ENUM("NONE", "HARDCODED", "STRING_CONCAT", "PARAMATIZED_QUERY_AND_CONCAT", "PARAMATIZED_QUERY") NULL,
  `proc_usage` ENUM("NONE", "HARDCODED", "STRING_CONCAT", "PARAMATIZED_QUERY_AND_CONCAT", "PARAMATIZED_QUERY") NULL,
  `fun_usage` ENUM("NONE", "HARDCODED", "STRING_CONCAT", "PARAMATIZED_QUERY_AND_CONCAT", "PARAMATIZED_QUERY") NULL,
  `event_usage` ENUM("NONE", "HARDCODED", "STRING_CONCAT", "PARAMATIZED_QUERY_AND_CONCAT", "PARAMATIZED_QUERY") NULL,
  `trig_usage` ENUM("NONE", "HARDCODED", "STRING_CONCAT", "PARAMATIZED_QUERY_AND_CONCAT", "PARAMATIZED_QUERY") NULL,
  `index_usage` ENUM("NONE", "HARDCODED", "STRING_CONCAT", "PARAMATIZED_QUERY_AND_CONCAT", "PARAMATIZED_QUERY") NULL,
  `db_usage` ENUM("NONE", "HARDCODED", "STRING_CONCAT", "PARAMATIZED_QUERY_AND_CONCAT", "PARAMATIZED_QUERY") NULL,
  `server_usage` ENUM("NONE", "HARDCODED", "STRING_CONCAT", "PARAMATIZED_QUERY_AND_CONCAT", "PARAMATIZED_QUERY") NULL,
  `tspace_usage` ENUM("NONE", "HARDCODED", "STRING_CONCAT", "PARAMATIZED_QUERY_AND_CONCAT", "PARAMATIZED_QUERY") NULL,
  PRIMARY KEY (`id`, `project`, `file`),
  INDEX `fk_analyses_projects1_idx` (`project` ASC) VISIBLE,
  INDEX `fk_analyses_files1_idx` (`file` ASC) VISIBLE,
  CONSTRAINT `fk_analyses_projects1`
    FOREIGN KEY (`project`)
    REFERENCES `crawler`.`projects` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_analyses_files1`
    FOREIGN KEY (`file`)
    REFERENCES `crawler`.`files` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
