package usf.edu.bronie.sqlcrawler.model;

public class CodeStatData {

    private long mParamQuery = 0;

    private long mStringConcat = 0;

    private long mHardCoded = 0;

    private long mParamQueryAndStringConcat = 0;


    public void incParamQuery() {
        mParamQuery++;
    }

    public void incStringConcat() {
        mStringConcat++;
    }

    public void incHardCoded() {
        mHardCoded++;
    }

    public void incParamQueryAndStringConcat() {
        mParamQueryAndStringConcat++;
    }

    public long getParamQuery() {
        return mParamQuery;
    }

    public long getStringConcat() {
        return mStringConcat;
    }

    public long getHardCoded() {
        return mHardCoded;
    }

    public long getParamQueryAndStringConcat() {
        return mParamQueryAndStringConcat;
    }
}