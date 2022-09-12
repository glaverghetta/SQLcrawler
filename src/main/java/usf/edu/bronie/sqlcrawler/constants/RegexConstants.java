package usf.edu.bronie.sqlcrawler.constants;

public class RegexConstants {

	public enum Languages {
		JAVA,
		CSHARP,
		PHP
	}
    // SQL Analyzers
	public static final String WHITESPACE = "(\\r\\n|\\r|\\n|\\t| )*";
    
	public static final String JAVA_CONCAT = "\\+";
	public static final String PHP_CONCAT = "\\.";
	
	public static final String JAVA_VARIABLE = "";
	public static final String PHP_VARIABLE = "\\$";
	// Concatenation with variable has the form " + var_name
    public static final String CONCAT_VAR = WHITESPACE + "(\\\'|\\\")(?=" + WHITESPACE + "%s" + WHITESPACE + "%s" + "[_a-zA-Z][_a-zA-Z0-9]*)";
    	
    // Concatenation with multiple variable has the form x , " + var_name
    public static final String CONCAT_VAR_MULTIPLE = WHITESPACE + "(?=[_a-zA-Z][_a-zA-Z0-9]*" + WHITESPACE + "\\," + CONCAT_VAR + ")";
	
    public static final String STRING_LITERAL_CONCAT_WITH_VAR = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*\\\"(?=(\\r\\n\\|\\r\\|\\n|\\t| )*\\+(\\r\\n\\|\\r\\|\\n|\\t| )*[_a-zA-Z][_a-zA-Z0-9]*)";

    public static final String STRING_LITERAL_CONCAT_WITH_VAR_LOWER = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*\\\"(?=(\\r\\n\\|\\r\\|\\n|\\t| )*\\+(\\r\\n\\|\\r\\|\\n|\\t| )*[a-z][_a-zA-Z0-9]*)";

    public static final String GROUP_ORDER_BY = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*(ORDER BY|GROUP BY)";

    public static final String TABLE = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*(FROM|INTO|UPDATE|JOIN|((create|alter|drop|rename|truncate) TABLE))";
    
    public static final String STRING_LITERAL_CONCAT_WITH_TABLE_LOWER = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*(FROM|INTO|UPDATE|JOIN|((create|alter|drop|rename|truncate) TABLE))(\\r\\n|\\r|\\n|\\t| )*\\\"(?=(\\r\\n|\\r|\\n|\\t| )*\\+(\\r\\n|\\r|\\n|\\t| )*[a-z][_a-zA-Z0-9]*)";

    public static final String STRING_LITERAL_COLUMN = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*(SELECT|SELECT DISTINCT|((alter|drop) COLUMN)|WHERE)";

    public static final String STRING_LITERAL_CONCAT_WITH_VIEW = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*((create|drop|alter) VIEW)(\\r\\n|\\r|\\n|\\t| )*\\\"(?=(\\r\\n|\\r|\\n|\\t| )*\\+(\\r\\n|\\r|\\n|\\t| )*[_a-zA-Z][_a-zA-Z0-9]*)";

    public static final String STRING_LITERAL_CONCAT_WITH_TSPACE = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*((create|drop|alter) TABLESPACE)(\\r\\n|\\r|\\n|\\t| )*\\\"(?=(\\r\\n|\\r|\\n|\\t| )*\\+(\\r\\n|\\r|\\n|\\t| )*[_a-zA-Z][_a-zA-Z0-9]*)";

    public static final String STRING_LITERAL_CONCAT_WITH_PROC = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*((create|drop|alter) PROCEDURE (IF EXIST|))(\\r\\n|\\r|\\n|\\t| )*\\\"(?=(\\r\\n|\\r|\\n|\\t| )*\\+(\\r\\n|\\r|\\n|\\t| )*[_a-zA-Z][_a-zA-Z0-9]*)";

    public static final String STRING_LITERAL_CONCAT_WITH_FUN = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*((create|drop|alter) FUNCTION (IF EXIST|))(\\r\\n|\\r|\\n|\\t| )*\\\"(?=(\\r\\n|\\r|\\n|\\t| )*\\+(\\r\\n|\\r|\\n|\\t| )*[_a-zA-Z][_a-zA-Z0-9]*)";

    public static final String STRING_LITERAL_CONCAT_WITH_EVENT = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*((create|drop|alter) EVENT (IF EXIST|))(\\r\\n|\\r|\\n|\\t| )*\\\"(?=(\\r\\n|\\r|\\n|\\t| )*\\+(\\r\\n|\\r|\\n|\\t| )*[_a-zA-Z][_a-zA-Z0-9]*)";

    public static final String STRING_LITERAL_CONCAT_WITH_SERVER = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*((create|drop|alter) SERVER (IF EXIST|))(\\r\\n|\\r|\\n|\\t| )*\\\"(?=(\\r\\n|\\r|\\n|\\t| )*\\+(\\r\\n|\\r|\\n|\\t| )*[_a-zA-Z][_a-zA-Z0-9]*)";

    public static final String STRING_LITERAL_CONCAT_WITH_DB = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*((create|drop|alter) (DATABASE|SCHEMA) (IF EXIST|IF NOT EXIST|))(\\r\\n|\\r|\\n|\\t| )*\\\"(?=(\\r\\n|\\r|\\n|\\t| )*\\+(\\r\\n|\\r|\\n|\\t| )*[_a-zA-Z][_a-zA-Z0-9]*)";

    public static final String STRING_LITERAL_CONCAT_WITH_INDEX = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*((create|drop|alter) INDEX)(\\r\\n|\\r|\\n|\\t| )*\\\"(?=(\\r\\n|\\r|\\n|\\t| )*\\+(\\r\\n|\\r|\\n|\\t| )*[_a-zA-Z][_a-zA-Z0-9]*)";

    public static final String STRING_LITERAL_CONCAT_WITH_TRIGGER = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*((create|drop) TRIGGER)(\\r\\n|\\r|\\n|\\t| )*\\\"(?=(\\r\\n|\\r|\\n|\\t| )*\\+(\\r\\n|\\r|\\n|\\t| )*[_a-zA-Z][_a-zA-Z0-9]*)";

    public static final String STRING_LITERAL_CONCAT_WITH_LIKE = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*LIKE(\\r\\n|\\r|\\n|\\t|%|'%|'| )*\\\"(?=(\\r\\n|\\r|\\n|\\t| )*\\+(\\r\\n|\\r|\\n|\\t| )*[_a-zA-Z][_a-zA-Z0-9]*)";

    public static final String STRING_LITERAL_PREP_STATE_LIKE = "LIKE(\\r\\n|\\r|\\n|\\t| )*(\\?|:.*?\\W)(\\r\\n|\\r|\\n|\\t| )*";

    public static final String STRING_LITERAL = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*\\\"";

    public static final String STRING_FORMAT = "%(s|d|h|a|b|c|e|f|g|n|o|t|x)";

    public static final String APPEND = "append(\\r\\n|\\r|\\n|\\t| )*\\((\\r\\n|\\r|\\n|\\t| )*[_a-zA-Z][_a-zA-Z0-9]*(\\r\\n|\\r|\\n|\\t| )*\\)";

    public static final String APPEND_LOWER = "append(\\r\\n|\\r|\\n|\\t| )*\\((\\r\\n|\\r|\\n|\\t| )*[a-z][_a-zA-Z0-9]*(\\r\\n|\\r|\\n|\\t| )*\\)";

    public static final String PREPARED_STATEMENT_KEYWORD = "?";

    public static final String PREPARED_STATEMENT_KEYWORD_JPA = "(\\r\\n|\\r|\\n|\\t| ):.*?\\W";

    public static final String[] GROUP_ORDER_BY_KEYWORD = {"order by", "group by"};

    public static final String[] COLUMN_KEYWORD = {"select", "select distinct", "alter column", "drop column"};

    public static final String[] TABLE_KEYWORD = {"from", "into", "update", "join", "create table", "alter table",
            "drop table", "rename table", "truncate table"};

    public static final String[] VIEW_KEYWORD = {"create view", "drop view", "alter view"};

    public static final String[] TSPACE_KEYWORD = {"create tablespace", "drop tablespace", "alter tablespace"};

    public static final String[] PROC_KEYWORD = {"create procedure", "drop procedure", "alter procedure"};

    public static final String[] FUN_KEYWORD = {"create function", "drop function", "alter function"};

    public static final String[] INDEX_KEYWORD = {"create index", "drop index", "alter index"};

    public static final String[] EVENT_KEYWORD = {"create event", "drop event", "alter event"};

    public static final String[] SERVER_KEYWORD = {"create server", "drop server", "alter server"};

    public static final String[] DB_KEYWORD = {"create database", "drop database", "alter database",
            "create schema", "drop schema", "alter schema"};

    public static final String[] TRIGGER_KEYWORD = {"create trigger", "drop trigger"};

    public static final String[] LIKE_KEYWORD = {"like"};

    public static final String STRING_FORMAT_KEYWORD = "String.format";

    // SQL Keywords
    public static final String[] SQL_KEYWORDS = {"select", "update", "delete", "insert into",
            "create database", "alter database", "create table", "alter table", "drop table",
            "create index", "drop index", "order by", "group by", "desc", "asc", "join", "where"};


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
    
    public static final String JAVA_SEARCH_TERMS = "executeQuery";
    
    //Old regular expressions
    //public static final String STRING_LITERAL_CONCAT_WITH_GROUP_ORDER_BY = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*(ORDER BY|GROUP BY)(\\r\\n|\\r|\\n|\\t| )*\\\"(?=(\\r\\n|\\r|\\n|\\t| )*\\+(\\r\\n|\\r|\\n|\\t| )*[_a-zA-Z][_a-zA-Z0-9]*)";
    //public static final String STRING_LITERAL_CONCAT_WITH_TABLE = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*(FROM|INTO|UPDATE|JOIN|((create|alter|drop|rename|truncate) TABLE))(\\r\\n|\\r|\\n|\\t| )*\\\"(?=(\\r\\n|\\r|\\n|\\t| )*\\+(\\r\\n|\\r|\\n|\\t| )*[_a-zA-Z][_a-zA-Z0-9]*)";
    //public static final String STRING_LITERAL_CONCAT_WITH_TABLE_LOWER = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*(FROM|INTO|UPDATE|JOIN|((create|alter|drop|rename|truncate) TABLE))(\\r\\n|\\r|\\n|\\t| )*\\\"(?=(\\r\\n|\\r|\\n|\\t| )*\\+(\\r\\n|\\r|\\n|\\t| )*[a-z][_a-zA-Z0-9]*)";
    //public static final String STRING_LITERAL_CONCAT_WITH_COLUMN = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*(SELECT|SELECT DISTINCT|((alter|drop) COLUMN)|WHERE)(\\r\\n|\\r|\\n|\\t| )*\\\"(?=(\\r\\n|\\r|\\n|\\t| )*\\+(\\r\\n|\\r|\\n|\\t| )*[_a-zA-Z][_a-zA-Z0-9]*)";

    //Unused regular expressions
    //public static final String STRING_LITERAL_CONCAT_WITH_TABLE = TABLE + CONCAT_VAR;
    //public static final String STRING_LITERAL_CONCAT_MULTIPLE_WITH_TABLE = TABLE + CONCAT_VAR_MULTIPLE;
  //public static final String STRING_LITERAL_CONCAT_WITH_GROUP_ORDER_BY = GROUP_ORDER_BY + CONCAT_VAR;
    //public static final String STRING_LITERAL_CONCAT_MULTIPLE_WITH_GROUP_ORDER_BY = GROUP_ORDER_BY + CONCAT_VAR_MULTIPLE;	
}
