# Only files with analyses (need to ignore files that become private or otherwise inaccessible)
SELECT * FROM files f WHERE f.id in (SELECT file FROM analyses a);

# All files grouped by hash
SELECT fileName, hash, COUNT(hash) as cnt FROM files GROUP BY hash;

# First file with an analysis for each hash
SELECT hash, MIN(id) as fileID, fileName, COUNT(hash) as cnt 
FROM (
	SELECT * FROM files f WHERE f.id in (SELECT file FROM analyses a)
) as derived 
GROUP BY hash;

# Save this result into a temporary table
CREATE TEMPORARY TABLE file_with_analysis_by_hash
SELECT hash, MIN(id) as fileID, fileName, COUNT(hash) as cnt 
FROM (
	SELECT * FROM files f WHERE f.id in (SELECT file FROM analyses a)
) as derived 
GROUP BY hash;

# Count the number of files in the temporary table
SELECT COUNT(fileID) FROM file_with_analysis_by_hash;

# Get the analysis data for all of these files
SELECT a.id as id, a.project, f.fileID as file, f.hash, f.fileName, f.cnt as numFilesWithHash, 
	a.analysis_date, a.sql_usage, a.is_parameterized, a.api_type, a.sql_usage_lower, 
    a.order_group_usage, a.like_usage, a.column_usage, a.table_usage, a.table_usage_lower, a.view_usage, a.proc_usage, 
    a.fun_usage, a.event_usage, a.trig_usage, a.index_usage, a.db_usage, a.server_usage, a.tspace_usage
FROM file_with_analysis_by_hash f
LEFT JOIN analyses a ON f.fileID = a.file;

# Create a table to store this information based on the analyses table - note the three extra columns hash, fileName, and numFilesWithHash
CREATE TABLE IF NOT EXISTS `unique_analyses` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project` INT NOT NULL,
  `file` INT NOT NULL,
  `hash` VARCHAR(45) NULL DEFAULT NULL,
  `filename` VARCHAR(200) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_0900_as_cs' NULL DEFAULT NULL,
  `numFilesWithHash` INT NULL DEFAULT NULL,
  `analysis_date` DATETIME NULL DEFAULT NULL,
  `sql_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY', 'STRING_CONCAT_LIST', 'STRING_INTERP') NULL DEFAULT NULL,
  `is_parameterized` TINYINT(1) NULL DEFAULT NULL,
  `api_type` VARCHAR(45) NULL DEFAULT NULL,
  `sql_usage_lower` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY', 'STRING_CONCAT_LIST', 'STRING_INTERP') NULL DEFAULT NULL,
  `order_group_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY', 'STRING_CONCAT_LIST', 'STRING_INTERP') NULL DEFAULT NULL,
  `like_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY', 'STRING_CONCAT_LIST', 'STRING_INTERP') NULL DEFAULT NULL,
  `column_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY', 'STRING_CONCAT_LIST', 'STRING_INTERP') NULL DEFAULT NULL,
  `table_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY', 'STRING_CONCAT_LIST', 'STRING_INTERP') NULL DEFAULT NULL,
  `table_usage_lower` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY', 'STRING_CONCAT_LIST', 'STRING_INTERP') NULL DEFAULT NULL,
  `view_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY', 'STRING_CONCAT_LIST', 'STRING_INTERP') NULL DEFAULT NULL,
  `proc_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY', 'STRING_CONCAT_LIST', 'STRING_INTERP') NULL DEFAULT NULL,
  `fun_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY', 'STRING_CONCAT_LIST', 'STRING_INTERP') NULL DEFAULT NULL,
  `event_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY', 'STRING_CONCAT_LIST', 'STRING_INTERP') NULL DEFAULT NULL,
  `trig_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY', 'STRING_CONCAT_LIST', 'STRING_INTERP') NULL DEFAULT NULL,
  `index_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY', 'STRING_CONCAT_LIST', 'STRING_INTERP') NULL DEFAULT NULL,
  `db_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY', 'STRING_CONCAT_LIST', 'STRING_INTERP') NULL DEFAULT NULL,
  `server_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY', 'STRING_CONCAT_LIST', 'STRING_INTERP') NULL DEFAULT NULL,
  `tspace_usage` ENUM('NONE', 'HARDCODED', 'STRING_CONCAT', 'PARAMATIZED_QUERY_AND_CONCAT', 'PARAMATIZED_QUERY', 'STRING_CONCAT_LIST', 'STRING_INTERP') NULL DEFAULT NULL,
  PRIMARY KEY (`id`, `project`, `file`),
  INDEX `fk_unique_analyses_projects1_idx` (`project` ASC) VISIBLE,
  INDEX `fk_unique_analyses_files1_idx` (`file` ASC) VISIBLE,
  CONSTRAINT `fk_unique_analyses_files1`
    FOREIGN KEY (`file`)
    REFERENCES `files` (`id`),
  CONSTRAINT `fk_unique_analyses_projects1`
    FOREIGN KEY (`project`)
    REFERENCES `projects` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;

# Insert the data into our newly created table
INSERT INTO unique_analyses
SELECT a.id as id, a.project, f.fileID as file, f.hash, f.fileName, f.cnt as numFilesWithHash, 
	a.analysis_date, a.sql_usage, a.is_parameterized, a.api_type, a.sql_usage_lower, 
    a.order_group_usage, a.like_usage, a.column_usage, a.table_usage, a.table_usage_lower, a.view_usage, a.proc_usage, 
    a.fun_usage, a.event_usage, a.trig_usage, a.index_usage, a.db_usage, a.server_usage, a.tspace_usage
FROM file_with_analysis_by_hash f
LEFT JOIN analyses a ON f.fileID = a.file;

# Confirm no hashes with different analyses
-- CREATE TABLE analysis_with_hash SELECT a.id, a.sql_usage, a.column_usage, a.table_usage, a.like_usage, f.id as file, f.hash, f.url FROM analyses a LEFT JOIN files f ON f.id=a.file;
-- -- SELECT a.id, a.file, f.url, a2.id as other_analysis, a2.file as other_file, a2.url as other_url, f.hash
-- SELECT COUNT(DISTINCT f.id)
-- FROM analyses a 
-- 	LEFT JOIN files f ON f.id=a.file 
--     LEFT JOIN analysis_with_hash a2 ON f.hash = a2.hash 
--     WHERE a.sql_usage != a2.sql_usage OR a.column_usage != a2.column_usage OR a.table_usage != a2.table_usage OR a.like_usage != a2.like_usage
--     LIMIT 10; -- Should return nothing (and thus might be slooooow)!
-- DROP TABLE analysis_with_hash;