package usf.edu.bronie.sqlcrawler.constants;

public class RegexConstants {

    // SQL Analyzers
    public static final String STRING_LITERAL_CONCAT_WITH_VAR = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*\\\"(?=(\\r\\n\\|\\r\\|\\n|\\t| )*\\+(\\r\\n\\|\\r\\|\\n|\\t| )*[_a-zA-Z][_a-zA-Z0-9]*)";

    public static final String STRING_LITERAL_CONCAT_WITH_ORDER_BY = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*ORDER BY(\\r\\n|\\r|\\n|\\t| )*\\\"(?=(\\r\\n|\\r|\\n|\\t| )*\\+(\\r\\n|\\r|\\n|\\t| )*[_a-zA-Z][_a-zA-Z0-9]*)";

    public static final String STRING_LITERAL_CONCAT_WITH_GROUP_BY = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*GROUP BY(\\r\\n|\\r|\\n|\\t| )*\\\"(?=(\\r\\n|\\r|\\n|\\t| )*\\+(\\r\\n|\\r|\\n|\\t| )*[_a-zA-Z][_a-zA-Z0-9]*)";

    public static final String STRING_LITERAL_CONCAT_WITH_FROM_INTO = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*(FROM|INTO|UPDATE)(\\r\\n|\\r|\\n|\\t| )*\\\"(?=(\\r\\n|\\r|\\n|\\t| )*\\+(\\r\\n|\\r|\\n|\\t| )*[_a-zA-Z][_a-zA-Z0-9]*)";

    public static final String STRING_LITERAL_CONCAT_WITH_LIKE = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*LIKE(\\r\\n|\\r|\\n|\\t|%|'%|'| )*\\\"(?=(\\r\\n|\\r|\\n|\\t| )*\\+(\\r\\n|\\r|\\n|\\t| )*[_a-zA-Z][_a-zA-Z0-9]*)";

    public static final String STRING_LITERAL_PREP_STATE_LIKE = "LIKE(\\r\\n|\\r|\\n|\\t| )*(\\?|:.*?\\W)(\\r\\n|\\r|\\n|\\t| )*";

    public static final String STRING_LITERAL = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*\\\"";

    public static final String STRING_FORMAT = "%(s|d|h|a|b|c|e|f|g|n|o|t|x)";

    public static final String APPEND = "append(\\r\\n|\\r|\\n|\\t| )*\\((\\r\\n|\\r|\\n|\\t| )*[_a-zA-Z][_a-zA-Z0-9]*(\\r\\n|\\r|\\n|\\t| )*\\)";

    public static final String PREPARED_STATEMENT_KEYWORD = "?";

    public static final String PREPARED_STATEMENT_KEYWORD_JPA = "(\\r\\n|\\r|\\n|\\t| ):.*?\\W";

    public static final String ORDER_BY_KEYWORD = "order by";

    public static final String GROUP_BY_KEYWORD = "group by";

    public static final String FROM_KEYWORD = "from";

    public static final String INTO_KEYWORD = "into";

    public static final String UPDATE_KEYWORD = "update";

    public static final String LIKE_KEYWORD = "like";

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

    public static final String IMPORT_JDBC = "java.sql";

    public static final String IMPORT_SPRING = "org.springframework";

    public static final String IMPORT_JPA = "javax.persistence";

    public static final String IMPORT_HIBERNATE = "org.hibernate";
}
