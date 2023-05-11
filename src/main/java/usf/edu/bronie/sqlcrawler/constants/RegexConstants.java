package usf.edu.bronie.sqlcrawler.constants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RegexConstants {

    private static final Logger log = LogManager.getLogger(RegexConstants.class);

    public enum Languages {
        JAVA("Java", "java"),
        CSHARP("C#", "cs"),
        PHP("PHP", "php"),
    	JS("JavaScript", "js");

        private final String searchString;
        private final String extension;

        private Languages(final String searchString, String extension) {
            this.searchString = searchString;
            this.extension = extension;
        }

        public String searchString() {
            return this.searchString;
        }

        public String extension() {
            return this.extension;
        }

        public static Languages extensionToLang(String ext) {
            switch (ext) {
                case "java":
                    return Languages.JAVA;
                case "cs":
                    return Languages.CSHARP;
                case "php": // TODO: PHP has a bunch of junk extensions. Can't use extensions like this,
                            // at least for PHP
                    return Languages.PHP;
                case "js":
                	return Languages.JS;
            }
            log.error("Unhandled file extension {}", ext);
            System.exit(-1);
            return Languages.JAVA;
        }

        public static Languages nameToLang(String name) {
            switch (name.toLowerCase()) {
                case "java":
                    return Languages.JAVA;
                case "php":
                    return Languages.PHP;
                case "c#":
                case "cs":
                    return Languages.CSHARP;
                case "js":
                	return Languages.JS;
            }
            log.error("Unrecognizable file type ({})", name.toLowerCase());
            System.exit(-1);
            return Languages.JAVA;
        }
        
    }

    // SQL Analyzers
    public static final String WHITESPACE = "(\\r\\n|\\r|\\n|\\t| )*";
    
    public static final String QUOTE = "(\\\"|\\`|\\\')";

    public static final String JAVA_CONCAT = "\\+";
    public static final String PHP_CONCAT = "\\.";
    public static final String CSHARP_CONCAT = "\\+";
    public static final String JS_CONCAT = "\\+";

    public static final String JAVA_VARIABLE = "[_a-zA-Z][_a-zA-Z0-9]*";
    public static final String PHP_VARIABLE = "\\$[_a-zA-Z][_a-zA-Z0-9]*";
    public static final String CSHARP_VARIABLE = "[_a-zA-Z][_a-zA-Z0-9]*";
    public static final String JS_VARIABLE = "[_a-zA-Z][_a-zA-Z0-9]*";
    
    public static final String JS_VARIABLE_STRING_INTERPOLATION = "\\$\\{[_a-zA-Z][_a-zA-Z0-9]*\\}"; // ${var}
    public static final String PHP_VARIABLE_STRING_INTERPOLATION =  "(\\{|)\\$[_a-zA-Z][_a-zA-Z0-9]*(\\}|)"; // $var OR {$var}
    // public static final String PHP_VARIABLE_STRING_INTERPOLATION = "(\\{||%)?\\$[_a-zA-Z][_a-zA-Z0-9]*(\\}||%)?"; // $var OR {$var} OR %$var%
    public static final String JAVA_VARIABLE_STRING_INTERPOLATION = "\\$?\\{[_a-zA-Z0-9]+\\}"; // ${var} or {var}
    public static final String CSHARP_VARIABLE_STRING_INTERPOLATION = "\\{.*\\}"; //{var} or {var()}
    
    // Concatenation with variable has the form " + var_name
    public static final String CONCAT_VAR = WHITESPACE + QUOTE + "(?=" + WHITESPACE + "%s" + WHITESPACE + "%s" + ")";
    
    // Interpolation
    public static final String INTERPOLATION_VAR = WHITESPACE + "%s" + WHITESPACE;


    // Concatenation with multiple variable has the form x , " + var_name
    // First %s is a VARIABLE. Note CONCAT_VAR has two %s to fill
    public static final String CONCAT_VAR_MULTIPLE = WHITESPACE + "(?=" + "%s"
            + WHITESPACE + "\\," + CONCAT_VAR + ")";

    public static final String STRING_LITERAL_CONCAT_WITH_VAR = QUOTE + "[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*\\\"(?="
            + WHITESPACE + "%s" + WHITESPACE + "%s" + ")";

    public static final String STRING_LITERAL_CONCAT_WITH_VAR_LOWER = QUOTE + "[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*" + QUOTE + "(?="
            + WHITESPACE + "%s" + WHITESPACE + "%s" + ")";
    
    public static final String STRING_LITERAL_STRING_INTERPOLATION_CONCAT = QUOTE + "[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*(?="
            + WHITESPACE + "%s" + WHITESPACE + ")";

    // public static final String STRING_LITERAL_CONCAT_WITH_VAR_LOWER =
    // "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*\\\"(?="
    // + WHITESPACE + "\\" + "%s" + WHITESPACE + "%s" + ")";

    public static final String GROUP_ORDER_BY = QUOTE + "[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*(ORDER BY|GROUP BY)";

    public static final String TABLE = QUOTE + "[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*(FROM|INTO|UPDATE|JOIN|((create|alter|drop|rename|truncate) TABLE))";

    public static final String STRING_LITERAL_CONCAT_WITH_TABLE_LOWER = QUOTE + "[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*(FROM|INTO|UPDATE|JOIN|((create|alter|drop|rename|truncate) TABLE))";

    public static final String STRING_LITERAL_COLUMN = QUOTE + "[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*(SELECT|SELECT DISTINCT|((alter|drop) COLUMN)|WHERE)";

    public static final String STRING_LITERAL_CONCAT_WITH_VIEW = QUOTE + "[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*((create|drop|alter)"
    		+ WHITESPACE + "VIEW)";

    public static final String STRING_LITERAL_CONCAT_WITH_TSPACE = QUOTE + "[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*((create|drop|alter)"
    		+ WHITESPACE + "TABLESPACE)";

    public static final String STRING_LITERAL_CONCAT_WITH_PROC = QUOTE + "[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*((create|drop|alter)"
    		+ WHITESPACE + "PROCEDURE" + WHITESPACE + "(IF EXIST|))";

    public static final String STRING_LITERAL_CONCAT_WITH_FUN = QUOTE + "[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*((create|drop|alter)"
    		+ WHITESPACE + "FUNCTION" + WHITESPACE + "(IF EXIST|))";
    
    public static final String STRING_LITERAL_CONCAT_WITH_EVENT = QUOTE + "[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*((create|drop|alter)"
    		+ WHITESPACE + "EVENT"+ WHITESPACE + "(IF EXIST|))";

    public static final String STRING_LITERAL_CONCAT_WITH_SERVER = QUOTE + "[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*((create|drop|alter)"
    		+ WHITESPACE + "SERVER" + WHITESPACE + "(IF EXIST|))";

    public static final String STRING_LITERAL_CONCAT_WITH_DB = QUOTE + "[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*((create|drop|alter)"
    + WHITESPACE +"(DATABASE|SCHEMA)" + WHITESPACE + "(IF EXIST|IF NOT EXIST|))";
    
    public static final String STRING_LITERAL_CONCAT_WITH_INDEX = QUOTE + "[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*((create|drop|alter)"
    + WHITESPACE + "INDEX)";

    public static final String STRING_LITERAL_CONCAT_WITH_TRIGGER = QUOTE + "[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*((create|drop)"
    		+ WHITESPACE +  "TRIGGER)";

    //public static final String STRING_LITERAL_CONCAT_WITH_LIKE = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*LIKE(\\r\\n|\\r|\\n|\\t|%%|'%%|'| )*\\\"(?=";
    public static final String STRING_LITERAL_CONCAT_WITH_LIKE = QUOTE + "[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*LIKE(\\r\\n|\\r|\\n|\\t%%|'%%|'| )*";

    public static final String STRING_LITERAL_PREP_STATE_LIKE = "LIKE" + WHITESPACE + "(\\?|:.*?\\W)" + WHITESPACE
            + "";

    public static final String STRING_LITERAL = QUOTE + "[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*" + QUOTE;

    // Note this % is not passed to the format function, so it does not need to be
    // escaped
    public static final String STRING_FORMAT = "%(s|d|h|a|b|c|e|f|g|n|o|t|x)";

    public static final String APPEND = "append" + WHITESPACE + "\\(" + WHITESPACE + ""
            + WHITESPACE + "\\)";

    public static final String APPEND_LOWER = "append" + WHITESPACE + "\\(" + WHITESPACE + "[a-z][_a-zA-Z0-9]*"
            + WHITESPACE + "\\)";

    public static final String PREPARED_STATEMENT_KEYWORD = "?";

    public static final String PREPARED_STATEMENT_KEYWORD_JPA = "(\\r\\n|\\r|\\n|\\t| ):.*?\\W";

    public static final String[] GROUP_ORDER_BY_KEYWORD = { "order by", "group by" };

    public static final String[] COLUMN_KEYWORD = { "select", "select distinct", "alter column", "drop column" };

    public static final String[] TABLE_KEYWORD = { "from", "into", "update", "join", "create table", "alter table",
            "drop table", "rename table", "truncate table" };

    public static final String[] VIEW_KEYWORD = { "create view", "drop view", "alter view" };

    public static final String[] TSPACE_KEYWORD = { "create tablespace", "drop tablespace", "alter tablespace" };

    public static final String[] PROC_KEYWORD = { "create procedure", "drop procedure", "alter procedure" };

    public static final String[] FUN_KEYWORD = { "create function", "drop function", "alter function" };

    public static final String[] INDEX_KEYWORD = { "create index", "drop index", "alter index" };

    public static final String[] EVENT_KEYWORD = { "create event", "drop event", "alter event" };

    public static final String[] SERVER_KEYWORD = { "create server", "drop server", "alter server" };

    public static final String[] DB_KEYWORD = { "create database", "drop database", "alter database",
            "create schema", "drop schema", "alter schema" };

    public static final String[] TRIGGER_KEYWORD = { "create trigger", "drop trigger" };

    public static final String[] LIKE_KEYWORD = { "like" };

    public static final String STRING_FORMAT_KEYWORD = "String.format";

    // SQL Keywords
    public static final String[] SQL_KEYWORDS = { "select", "update", "delete", "insert into",
            "create database", "alter database", "create table", "alter table", "drop table",
            "create index", "drop index", "order by", "group by", "desc", "asc", "join", "where" };

    // String manipulation and analysis

    public static final String AFTER_LAST_SLASH = "([^\\/]+$)";

    public static final String GITHUB_STAR_PATTERN = "\\d+(?= user(s|) starred this repository)";

    public static final String GITHUB_WATCH_PATTERN = "\\d+(?= user(s|) (are|is) watching this repository)";

    public static final String GITHUB_FORK_PATTERN = "\\d+(?= user(s|) forked this repository)";

    public static final String GITHUB_FORK_FROM_PATTERN = "(?<=forked from <a href=\\\").+?(?=\\\">)";

    public static final String GITHUB_PROJECT_COMMIT_TIME_PATTERN = "(?<=datetime=\\\")\\d+(?=.*?relative-time>)";

    public static final String GITHUB_TOTAL_COMMIT_PATTERN = "\\d+(?= </span> commit(s|))";

    public static final String GITHUB_TOTAL_BRANCH_PATTERN = "\\d+(?= </span> branch(es|))";

    public static final String GITHUB_TOTAL_RELEASE_PATTERN = "\\d+(?= </span> release(s|))";

    public static final String GITHUB_TOTAL_CONT_PATTERN = "\\d+(?= </span> contributor(s|))";

    // php reminder
    public static final String IMPORT_JDBC = "java.sql";

    public static final String IMPORT_SPRING = "org.springframework";

    public static final String IMPORT_JPA = "javax.persistence";

    public static final String IMPORT_HIBERNATE = "org.hibernate";
    
    // C-SHARP imports
    public static final String USING_SQL = "using System.Data.SqlClient;";

    public static final String JAVA_SEARCH_TERMS = "executeQuery";
    
    // TO DO
    //public static final String PHP_SEARCH_TERMS = "multi_query";
    public static final String PHP_SEARCH_TERMS = "mysqli_query";
    
    // EcecuteNonQuery, ExecuteReader, ExecuteScalar in terms of popularity - all are used
    public static final String CSHARP_SEARCH_TERMS = "ExecuteScalar";
    
    // There is also execute() as a function & query
    public static final String JS_SEARCH_TERMS = "query createConnection";
    
    public static final String JAVA_PREP_STATEMENT_TERM = "prepareStatement";
    public static final String JS_PREP_STATEMENT_TERM = "?";
    public static final String PHP_PREP_STATEMENT_TERM = "->prepare";
    public static final String CSHARP_PREP_STATEMENT_TERM = "@";
    


    public static String getVariable(RegexConstants.Languages language) {
        switch (language) {
            case JAVA:
                return RegexConstants.JAVA_VARIABLE;
            case PHP:
                return RegexConstants.PHP_VARIABLE;
            case CSHARP:
            	return RegexConstants.CSHARP_VARIABLE;
            case JS:
            	return RegexConstants.JS_VARIABLE;
            default:
                log.error("Unhandled language requested {}", language);
                System.exit(-1);
        }
        return "";
    }

    public static String getConcat(RegexConstants.Languages language) {
        switch (language) {
            case JAVA:
                return RegexConstants.JAVA_CONCAT;
            case PHP:
                return RegexConstants.PHP_CONCAT;
            case CSHARP:
            	return RegexConstants.CSHARP_CONCAT;
            case JS:
            	return RegexConstants.JS_CONCAT;
            default:
                log.error("Unhandled language requested {}", language);
                System.exit(-1);
        }
        return "";
    }

    public static String getSearchTerms(RegexConstants.Languages language) {
        switch (language) {
            case JAVA:
                return RegexConstants.JAVA_SEARCH_TERMS;
            case PHP:
            	return RegexConstants.PHP_SEARCH_TERMS;
            case CSHARP:
            	return RegexConstants.CSHARP_SEARCH_TERMS;
            case JS:
            	return RegexConstants.JS_SEARCH_TERMS;

            default:
                log.error("Unhandled language requested {}", language);
                System.exit(-1);
        }
        return "";
    }
    
    public static String getPreparedStatementTerm(RegexConstants.Languages language) {
    	switch(language) {
    		case JAVA:
    			return RegexConstants.JAVA_PREP_STATEMENT_TERM;
    		case PHP:
    			return RegexConstants.PHP_PREP_STATEMENT_TERM;
    		case CSHARP:
    			return RegexConstants.CSHARP_PREP_STATEMENT_TERM;
    		case JS:
    			return RegexConstants.JS_PREP_STATEMENT_TERM;
    		default:
    			log.error("unhandled language requested {}", language);
    			System.exit(-1);
    	}
    	return "";
    }
    
    public static String getStringInterpolationTerm(RegexConstants.Languages language) {
    	switch(language) {
    		case JAVA:
    			return RegexConstants.JAVA_VARIABLE_STRING_INTERPOLATION;
    		case PHP:
    			return RegexConstants.PHP_VARIABLE_STRING_INTERPOLATION;
    		case CSHARP:
    			return RegexConstants.CSHARP_VARIABLE_STRING_INTERPOLATION;
    		case JS:
    			return RegexConstants.JS_VARIABLE_STRING_INTERPOLATION;
    		default:
    			log.error("unhandled language requested {}", language);
    			System.exit(-1);
    	}
    	return "";
    }
    
}
