package usf.edu.bronie.sqlcrawler.model;

/**
 * An enumeration representing the type of SQL usage found in a file.
 * 
 * For compatibility with the database, the enum begins at 1, not 0.
 */

public enum SQLType {
    PARAMATIZED_QUERY(5),
    PARAMATIZED_QUERY_AND_CONCAT(4),
    STRING_CONCAT_LIST(6),
    STRING_CONCAT(3),
    HARDCODED(2),
    NONE(1);

    private int mType;

    SQLType(int type) {
        mType = type;
    }

    public int toInt() {
        return mType;
    }
}
