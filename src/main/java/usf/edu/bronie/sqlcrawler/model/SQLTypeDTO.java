package usf.edu.bronie.sqlcrawler.model;

public class SQLTypeDTO {

    private SQLType mSQLType;

    private boolean mIsOrderByConcat;

    private boolean mIsLikePrep;

    public SQLTypeDTO(SQLType SQLType, boolean isOrderByConcat, boolean isLikePrep) {
        mSQLType = SQLType;
        mIsOrderByConcat = isOrderByConcat;
        mIsLikePrep = isLikePrep;
    }

    public SQLType getSQLType() {
        return mSQLType;
    }

    public boolean isOrderByConcat() {
        return mIsOrderByConcat;
    }

    public boolean isLikePrep() {
        return mIsLikePrep;
    }
}
