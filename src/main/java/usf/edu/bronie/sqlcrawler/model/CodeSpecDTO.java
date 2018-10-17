package usf.edu.bronie.sqlcrawler.model;

public class CodeSpecDTO {

    private String mCommitDate;

    private String mApiType;

    private String mProjectName;

    private String mFileUrl;

    private String mRawUrl;

    private SQLType mSQLUsage;

    private SQLType mLikeUsage;

    public CodeSpecDTO(String commitDate, String apiType, String projectName, String fileUrl, String rawUrl,
                       String SQLUsage, String likeUsage) {
        mCommitDate = commitDate;
        mApiType = apiType;
        mProjectName = projectName;
        mFileUrl = fileUrl;
        mRawUrl = rawUrl;
        mSQLUsage = toSQLType(SQLUsage);
        mLikeUsage = toSQLType(likeUsage);
    }

    public String getCommitDate() {
        return mCommitDate;
    }

    public ApiType getApiType() {
        return ApiType.valueOf(mApiType);
    }

    public String getProjectName() {
        return mProjectName;
    }

    public String getFileUrl() {
        return mFileUrl;
    }

    public String getRawUrl() {
        return mRawUrl;
    }

    public SQLType getSQLUsage() {
        return mSQLUsage;
    }

    public SQLType getLikeUsage() {
        return mLikeUsage;
    }

    private SQLType toSQLType(String sqlUsage) {
        return SQLType.valueOf(sqlUsage);
    }
}
