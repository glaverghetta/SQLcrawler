package usf.edu.bronie.sqlcrawler.constants;

public class RegexConstants {

    public static final String STRING_LITERAL_CONCAT_WITH_VAR = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*\\\"(?=( )*(\\r\\n\\|\\r\\|\\n)*( )*\\+( )*(\\r\\n\\|\\r\\|\\n)*( )*[_a-zA-Z][_a-zA-Z0-9]*)";

    public static final String STRING_LITERAL_CONCAT_WITH_LIKE = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*LIKE(\\r\\n|\\r|\\n|\\t|%| )*\\\"(?=(\\r\\n|\\r|\\n|\\t| )*\\+(\\r\\n|\\r|\\n|\\t| )*[_a-zA-Z][_a-zA-Z0-9]*)";

    public static final String STRING_LITERAL_PREP_STATE_LIKE = "LIKE(\\r\\n|\\r|\\n|\\t| )*\\?(\\r\\n|\\r|\\n|\\t| )*";

    public static final String STRING_LITERAL = "\\\"[^\\\"\\\\]*(\\\\.[^\\\"\\\\]*)*\\\"";

    public static final String APPEND = "append(\\r\\n|\\r|\\n)*\\((\\r\\n|\\r|\\n)*[_a-zA-Z][_a-zA-Z0-9]*(\\r\\n|\\r|\\n)*\\)";

    public static final String[] SQL_KEYWORDS = {"select", "update", "delete", "insert into",
            "create database", "alter database", "create table", "alter table", "drop table",
            "create index", "drop index", "order by", "group by", "desc", "asc", "join", "where", "show variables"};

    public static final String SPECIFIC_KEYWORD = "like";

    public static final String PREPARED_STATEMENT_KEYWORD = "?";


}

