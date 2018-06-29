package usf.edu.bronie.sqlcrawler.model;

public class SQLTypeDTO {

    private SQLType mSQLType;

    private boolean mIsOrderByConcat;

    public SQLTypeDTO(SQLType SQLType, boolean isOrderByConcat) {
        mSQLType = SQLType;
        mIsOrderByConcat = isOrderByConcat;
    }

    public SQLType getSQLType() {
        return mSQLType;
    }

    public boolean isOrderByConcat() {
        return mIsOrderByConcat;
    }
}
