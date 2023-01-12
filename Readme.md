# SQL Injection Vulnerability Analyzer V2

This tool is used to determine SQL usage in source files.
The tool takes an individual source file as input, and matches regular expressions to identify
how SQL statements are constructed in the source file.

The following languages are currently supported:

- Java
- PHP
- C#
- NodeJS

The tool is also capable of crawling Github for vulnerable files using the official Github API. The results of the
analysis are stored in a database to allow for fine-tuned querying (some useful queries are included below).

## Table of Contents

- [SQL Injection Vulnerability Analyzer V2](#sql-injection-vulnerability-analyzer-v2)
  - [Table of Contents](#table-of-contents)
  - [Compilation](#compilation)
  - [Logging, Debugging, and Performance Measures](#logging-debugging-and-performance-measures)
    - [Performance Log Files](#performance-log-files)
      - [GithubAPILogger](#githubapilogger)
      - [NetworkLogger](#networklogger)
      - [GithubThrottlingLogger](#githubthrottlinglogger)
      - [PageLogger](#pagelogger)
      - [FileLogger](#filelogger)
      - [AnalyzerLogger](#analyzerlogger)
      - [FrameLogger](#framelogger)
      - [FinalLogger](#finallogger)
  - [Implementation](#implementation)
  - [Database](#database)
    - [Useful Queries](#useful-queries)

## Compilation

A pom.xml file is provided for easy compilation, but a few steps must be taken. In the future, these may be runtime arguments or
otherwise specified after compilation.

First, create a copy of [CredentialConstants.java.example](src\main\java\usf\edu\bronie\sqlcrawler\constants\CredentialConstants.java.example) and remove the ".example" extension. If you are only analyzing an individual file, the following steps are optional.

The Credential Constants file contains the [Github Token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token) for querying the Github API and database credentials for storing the results.  

Follow the link to generate your own token and paste your token in the file.  For example:

    public static final String GITHUB_TOKEN = "ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"

Next, build the database using the crawlerdb.sql file. This file was designed and tested with MySQL.  The database schema is
described below. Now add the database credentials to the Credential Constants file, like so:

    public static final String DB_URL = "jdbc:mysql://localhost:3306/crawler";
    public static final String DB_USER = "kevin";
    public static final String DB_PASS = "SuperG00dPassword";

Now simply build with maven.

    mvn package

The jar file can be found in the target folder.  Sample usage:

    $ java -jar target/sqlcrawler-2.0-jar-with-dependencies.jar
    Usage: SQLCrawler [COMMAND]
    Tool for analyzing SQLIDIA vulnerabilities
    Commands:
      help      Displays help information about the specified command
      ...

## Logging, Debugging, and Performance Measures

The crawler uses log4j2 to implement logging. The logging can thus
be easily configured using the [log4j2.properties](src\main\resources\log4j2.properties) file.
After changing the logging file, simply rebuild with maven.

Line 2 allows for you to turn dependency logging on/off. For example, here are three potential values:

    rootLogger=info, stdout  # Info from dependencies
    rootLogger=off, stdout  # No logging from dependencies
    rootLogger=debug, stdout  # Debug logging from dependencies

Line 6 allows for you to control useful debugging/logging statements.

    logger.app.level = debug  # Enables debugging from the crawler app
    logger.app.level = off  # No logging at all from the crawler app

Lines 15-22 allow you to enable various log values for fine-tune tracking of runtime performance.
The various log files and their structures are described in the next section. To enable/disable a logger,
switch the off/info to info/off on the corresponding line (similar to line 2 and 6 above).

### Performance Log Files

To track the crawler performance, various runtime stats are collected. Each of the loggers and their purpose are described below.

The log files are named with the start time of the program. For example, `Final_2022-10-17-05-21-06.log`.
This allows for you to match the log files that are part of the same run if the program is run multiple times.

To limit clutter, log files are lazy. That is, they will only be created if the logging for that file is enabled and data is actually logged to them.

The log files are delimited with `' ~ '` (space tilde space). The files can then be exported into [excel](https://support.affinity.co/hc/en-us/articles/360044453711-How-to-open-CSV-files-with-the-correct-delimiter-separator) or a [database](https://stackoverflow.com/a/17071108) for analysis.

#### GithubAPILogger

The Github API logger tracks the execution time of requests to the Github API and parsing the resulting JSON data.

Failed requests are not included in this log, but can be found in the Network or Github Throttling (if failed due to an API limit exception) logger.

File format:

    Log Time ~ Start time ~ End time ~ Time Diff in ms ~ API endpoint ~ page ~ query string ~ content size in bytes ~ number of results

#### NetworkLogger

The Network logger tracks the time of HTTP requests.  That is, measures the time of the okhttp [execute function](https://square.github.io/okhttp/4.x/okhttp/okhttp3/-call/execute/).

File format:

    Log Time ~ Start time ~ End time ~ Time Diff in ms ~ URL ~ Return code

#### GithubThrottlingLogger

The Github Throttling logger records every time the application hits an API limit. This includes the [core and search](https://docs.github.com/en/rest/rate-limit#about-the-rate-limit-api) limits, and [secondary](https://docs.github.com/en/rest/overview/resources-in-the-rest-api#secondary-rate-limits) rate limiting. Search calls are computationally expensive, and pretty aggressively limited.

File format:

    Log Time ~ Limit Type ~ Reset Time ~ Wait Time in ms ~ Page number ~ Frame Min Size in bytes ~ Frame Max Size in bytes 

#### PageLogger

The Page logger tracks the time taken to query a specific page from the database and analyze the resulting files.

File format:

    Log Time ~ Start time ~ End time ~ Time Diff in ms ~ page number ~ frame start in bytes ~ frame end in bytes ~ number of files in page

#### FileLogger

The File logger tracks the time taken to download a file and run all analyzers.

File format:

    Log Time ~ Start time ~ End time ~ Time Diff in ms ~ File Database ID ~ File Size ~ page number ~ frame start in bytes ~ frame end in bytes

#### AnalyzerLogger

The Analyzer logger tracks the run time for each individual analyzer.  The analyzers are identified by their database column name.

File format:

    Log Time ~ Start time ~ End time ~ Time Diff in ms ~ Analyzer Name ~ File Database ID ~ result

#### FrameLogger

Since Github limits the number of files that be searched (see [Implementation](#implementation)), the crawler must split each search into chunks with less than 1000 results, which we do by specifying a size to search for in bytes.  For example, search for Java files with "executeQuery" with a size between 500 and 1000 bytes. This log files tracks these frames.

This log file tracks the time taken to pull all pages in a frame from the API and analyze them.

File format:

    Log Time ~ Start time ~ End time ~ Time Diff in ms ~ frame start in bytes ~ frame end in bytes ~ unique files analyzed ~ files reported by API ~ number of pages in frame ~ number of results reported by Github ~ number of times frame shrunk ~ a 1 if frame grew

#### FinalLogger

Unlike the other loggers, the final logger will only ever have two lines printed and aren't as easily parsed by machine. The first line is written immediately at the program start, and logs the command being run and the provided arguments. The second line is generated at the end of the program and includes the total run time and summary of results.

This file can be used to match the other files to the context they were created in.

Example:

    2022-10-17T05:21:06.800-0400 ~ Arguments provided: optimize ~ Java ~ 55 ~ --window ~ 100 ~ --start ~ 50
    2022-10-17T05:21:12.356-0400 ~ 38 ~ 1 ~ 2022-10-17T05:21:06.917-0400 ~ 2022-10-17T05:21:12.355-0400 ~ 5438

## Implementation

TODO: Add description of different command line options

Describe the search technique (shrinking window by size)

## Database

### Useful Queries

Project table is very generic, allowing for new sources to be added (for example, searchcode also searches Fedora Project)

Additional information for repos, such as stars and forks found in repo info table

Add to repo_info the "used by" or dependency graph on github

SELECT f.filename, a.* FROM crawler.analyses a LEFT JOIN crawler.projects p ON p.id = a.project LEFT JOIN crawler.files f ON f.id = a.file WHERE p.name="dummy/dummyRepo"

SELECT f.filename, a.*FROM (WITH dated_analyses AS (
  SELECT a.*, ROW_NUMBER() OVER (PARTITION BY file ORDER BY analysis_date DESC) AS rn
  FROM analyses AS a
)
SELECT * FROM dated_analyses WHERE rn = 1) as a LEFT JOIN crawler.projects p ON p.id = a.project LEFT JOIN crawler.files f ON f.id = a.file WHERE p.name="dummy/dummyRepo";
