# SQL Injection Vulnerability Analyzer

This tool is used to determine SQL usages in each source file.
This tool takes an individual Java source file as an input, and matches regular expressions to identify 
how SQL statements are constructed in a Java source file.

## USAGE

* A database containing link to the Java source files need to be created. To do so,
  * Database URL must be entered to the DBConnection class
* Database schema:  
 ````
 CREATE TABLE PROJECT_STATS (project_name varchar(255), param_count int, param_and_concat_count int, concat_count int, hardcode_count int, fork_count int, watch_count int, star_count int, last_commit_date varchar(255), forked_from varchar(255), total_commit int, total_branch int, total_release int, total_contr int) ENGINE=InnoDB DEFAULT CHARSET=latin1 DEFAULT COLLATE=latin1_swedish_ci;
 CREATE TABLE code_specs (project_name varchar(255), commit_date varchar(255), sql_usage varchar(255), orderby_usage varchar(255), group_usage varchar(255), like_usage varchar(255), from_into_usage varchar(255), api_type varchar(255), file_hash varchar(255), file_url varchar(255), raw_url varchar(255)) ENGINE=InnoDB DEFAULT CHARSET=latin1 DEFAULT COLLATE=latin1_swedish_ci;
 ````

* `GithubPageCrawler` and `GithubProjectCrawler` classes can crawl a GitHub project and extract source file specific information such as commit date, contributer count.


TODO: DB notes

Project table is very generic, allowing for new sources to be added (for example, searchcode also searches Fedora Project)

Additional information for repos, such as stars and forks found in repo info table

Add to repo_info the "used by" or dependency graph on github

Add your Github token to the Credentials file. Link the github page detailing it.