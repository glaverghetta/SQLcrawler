package usf.edu.bronie.sqlcrawler.model;

/**
 * TODO:
 * I believe this is an old enumeration replaced by SQLType.java.  I pulled it from an older version while testing.
 * Don't believe this is necessary.
 * 
 * An enumeration representing the type of SQL usage found in a file.
 * 
 */

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