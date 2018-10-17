package usf.edu.bronie.sqlcrawler.model;

public enum SQLType {
    PARAMATIZED_QUERY(4),
    PARAMATIZED_QUERY_AND_CONCAT(3),
    STRING_CONCAT(2),
    HARDCODED(1),
    NONE(0);

    private int mType;

    SQLType(int type) {
        mType = type;
    }

    public int toInt() {
        return mType;
    }
}
