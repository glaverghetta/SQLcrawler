package usf.edu.bronie.sqlcrawler.model;

public class GitFileAnalysisResultDto {

    private String mCommitDate;

    private SQLType mSQLUsage;

    private SQLType mOrderByUsage;

    private SQLType mGroupByUsage;

    private SQLType mLikeUsage;

    private SQLType mFromIntoUsage;

    private ApiType mApiType;

    private String mFileHash;

    private String mProjectName;

    private String mFileUrl;

    private String mRawUrl;

    private GitFileAnalysisResultDto(String projectName, String commitDate, SQLType SQLUsage, SQLType orderByUsage,
                             SQLType groupByUsage, SQLType likeUsage, SQLType fromIntoUsage, ApiType apiType, String fileHash,
                                     String fileUrl, String rawUrl) {
        mProjectName = projectName;
        mCommitDate = commitDate;
        mSQLUsage = SQLUsage;
        mOrderByUsage = orderByUsage;
        mGroupByUsage = groupByUsage;
        mLikeUsage = likeUsage;
        mFromIntoUsage = fromIntoUsage;
        mApiType = apiType;
        mFileHash = fileHash;
        mFileUrl = fileUrl;
        mRawUrl = rawUrl;
    }

    public String getProjectName() {
        return mProjectName;
    }

    public String getCommitDate() {
        return mCommitDate;
    }

    public SQLType getSQLUsage() {
        return mSQLUsage;
    }

    public SQLType getOrderByUsage() {
        return mOrderByUsage;
    }

    public SQLType getGroupByUsage() {
        return mGroupByUsage;
    }

    public SQLType getLikeUsage() {
        return mLikeUsage;
    }

    public ApiType getApiType() {
        return mApiType;
    }

    public String getFileHash() {
        return mFileHash;
    }

    public String getFileUrl() {
        return mFileUrl;
    }

    public String getRawUrl() {
        return mRawUrl;
    }

    public SQLType getFromIntoUsage() {
        return mFromIntoUsage;
    }

    public static class GitFileAnalysisResultDtoBuilder {
        private String mProjectName;
        private String mCommitDate = null;
        private SQLType mSQLUsage = SQLType.NONE;
        private SQLType mOrderByUsage = SQLType.NONE;
        private SQLType mGroupByUsage = SQLType.NONE;
        private SQLType mLikeUsage = SQLType.NONE;
        private SQLType mFromIntoUsage = SQLType.NONE;
        private ApiType mApiType = ApiType.NONE;
        private String mFileHash = null;
        private String mFileUrl;
        private String mRawUrl;

        public GitFileAnalysisResultDtoBuilder setProjectName(String projectName) {
            mProjectName = projectName;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setCommitDate(String commitDate) {
            mCommitDate = commitDate;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setSQLUsage(SQLType SQLUsage) {
            mSQLUsage = SQLUsage;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setOrderByUsage(SQLType orderByUsage) {
            mOrderByUsage = orderByUsage;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setGroupByUsage(SQLType groupByUsage) {
            mGroupByUsage = groupByUsage;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setLikeUsage(SQLType likeUsage) {
            mLikeUsage = likeUsage;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setFromIntoUsage(SQLType fromIntoUsage) {
            mFromIntoUsage = fromIntoUsage;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setApiType(ApiType apiType) {
            mApiType = apiType;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setFileHash(String fileHash) {
            mFileHash = fileHash;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setFileUrl(String fileUrl) {
            mFileUrl = fileUrl;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setRawUrl(String rawUrl) {
            mRawUrl = rawUrl;
            return this;
        }

        public GitFileAnalysisResultDto createGitFileAnalysisResultDto() {
            return new GitFileAnalysisResultDto(mProjectName, mCommitDate, mSQLUsage, mOrderByUsage, mGroupByUsage,
                    mLikeUsage, mFromIntoUsage, mApiType, mFileHash, mFileUrl, mRawUrl);
        }
    }
}
