package usf.edu.bronie.sqlcrawler.constants;

public class RegexConstants {

    // SQL Analyzers
    public static final String STRING_LITERAL_CONCAT_WITH_VAR = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*\\\"(?=( )*(\\r\\n\\|\\r\\|\\n)*( )*\\+( )*(\\r\\n\\|\\r\\|\\n)*( )*[_a-zA-Z][_a-zA-Z0-9]*)";

    public static final String STRING_LITERAL_CONCAT_WITH_ORDER_BY = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*ORDER BY(\\r\\n|\\r|\\n|\\t| )*\\\"(?=(\\r\\n|\\r|\\n|\\t| )*\\+(\\r\\n|\\r|\\n|\\t| )*[_a-zA-Z][_a-zA-Z0-9]*)";

    public static final String STRING_LITERAL_CONCAT_WITH_GROUP_BY = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*GROUP BY(\\r\\n|\\r|\\n|\\t| )*\\\"(?=(\\r\\n|\\r|\\n|\\t| )*\\+(\\r\\n|\\r|\\n|\\t| )*[_a-zA-Z][_a-zA-Z0-9]*)";

    public static final String STRING_LITERAL_CONCAT_WITH_LIKE = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*LIKE(\\r\\n|\\r|\\n|\\t|%|'%| )*\\\"(?=(\\r\\n|\\r|\\n|\\t| )*\\+(\\r\\n|\\r|\\n|\\t| )*[_a-zA-Z][_a-zA-Z0-9]*)";

    public static final String STRING_LITERAL_PREP_STATE_LIKE = "LIKE(\\r\\n|\\r|\\n|\\t| )*\\?(\\r\\n|\\r|\\n|\\t| )*";

    public static final String STRING_LITERAL = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*\\\"";

    public static final String APPEND = "append(\\r\\n|\\r|\\n)*\\((\\r\\n|\\r|\\n)*[_a-zA-Z][_a-zA-Z0-9]*(\\r\\n|\\r|\\n)*\\)";

    public static final String PREPARED_STATEMENT_KEYWORD = "?";

    public static final String ORDER_BY_KEYWORD = "order by";

    public static final String GROUP_BY_KEYWORD = "group by";

    // SQL Keywords
    public static final String[] SQL_KEYWORDS = {"select", "update", "delete", "insert into",
            "create database", "alter database", "create table", "alter table", "drop table",
            "create index", "drop index", "order by", "group by", "desc", "asc", "join", "where"};


    // String manipulation and analysis

    public static final String AFTER_LAST_SLASH = "([^\\/]+$)";

    public static final String GITHUB_STAR_PATTERN = "\\d+(?= user(s|) starred this repository)";

    public static final String GITHUB_WATCH_PATTERN = "\\d+(?= user(s|) (are|is) watching this repository)";

    public static final String GITHUB_FORK_PATTERN = "\\d+(?= user(s|) forked this repository)";

    public static final String GITHUB_COMMIT_TIME_PATTERN = "\\d+(?=</relative-time>)";
}
