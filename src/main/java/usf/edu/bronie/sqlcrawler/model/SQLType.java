package usf.edu.bronie.sqlcrawler.model;

public enum SQLType {
    PARAMATIZED_QUERY("PARAMATIZED_QUERY"),
    STRING_CONCAT("STRING_CONCAT"),
    PARAMATIZED_QUERY_AND_CONCAT("PARAMATIZED_QUERY_AND_CONCAT"),
    HARDCODED("HARDCODED"),
    NONE("NONE");

    private String mType;

    private SQLType(String type) {
        mType = type;
    }

    public String toString() {
        return mType;
    }
}
