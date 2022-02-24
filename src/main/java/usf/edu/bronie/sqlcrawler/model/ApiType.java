package usf.edu.bronie.sqlcrawler.model;

public enum ApiType {
    JDBC("JDBC"),
    HIBERNATE("HIBERNATE"),
    JPA("JPA"),
    SPRING("SPRING"),
    NONE("NONE");

    private String mType;

    private ApiType(String type) {
        mType = type;
    }

    @Override
    public String toString() {
        return mType;
    }
}
