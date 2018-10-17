package usf.edu.bronie.sqlcrawler.model;

public class GitFileAnalysisResultDto {

    private String mCommitDate;

    private ApiType mApiType;

    private String mProjectName;

    private String mFileUrl;

    private SQLType mSQLUsage;

    private SQLType mSQLUsageLower;

    private SQLType mLikeUsage;

    private SQLType mOrderGroupByUsage;

    private SQLType mColumnUsage;

    private SQLType mTableUsage;

    private SQLType mTableUsageLower;

    private SQLType mViewUsage;

    private SQLType mProcUsage;

    private SQLType mFunUsage;

    private SQLType mEventUsage;

    private SQLType mTriggerUsage;

    private SQLType mIndexUsage;

    private SQLType mDBUsage;

    private SQLType mServerUsage;

    private SQLType mTableSpaceUsage;

    private GitFileAnalysisResultDto(String commitDate, ApiType apiType, String projectName,
                                     String fileUrl, SQLType SQLUsage, SQLType SQLUsageLower, SQLType likeUsage, SQLType orderGroupByUsage,
                                     SQLType columnUsage, SQLType tableUsage, SQLType tableUsageLower, SQLType viewUsage, SQLType procUsage,
                                     SQLType funUsage, SQLType eventUsage, SQLType triggerUsage, SQLType indexUsage,
                                     SQLType DBUsage, SQLType serverUsage, SQLType tableSpaceUsage) {

        mCommitDate = commitDate;
        mApiType = apiType;
        mProjectName = projectName;
        mFileUrl = fileUrl;
        mSQLUsage = SQLUsage;
        mLikeUsage = likeUsage;
        mOrderGroupByUsage = orderGroupByUsage;
        mColumnUsage = columnUsage;
        mTableUsage = tableUsage;
        mViewUsage = viewUsage;
        mProcUsage = procUsage;
        mFunUsage = funUsage;
        mEventUsage = eventUsage;
        mTriggerUsage = triggerUsage;
        mIndexUsage = indexUsage;
        mDBUsage = DBUsage;
        mServerUsage = serverUsage;
        mTableSpaceUsage = tableSpaceUsage;
        mTableUsageLower = tableUsageLower;
        mSQLUsageLower = SQLUsageLower;
    }

    public String getCommitDate() {
        return mCommitDate;
    }

    public ApiType getApiType() {
        return mApiType;
    }

    public String getProjectName() {
        return mProjectName;
    }

    public String getFileUrl() {
        return mFileUrl;
    }

    public SQLType getSQLUsage() {
        return mSQLUsage;
    }

    public SQLType getLikeUsage() {
        return mLikeUsage;
    }

    public SQLType getOrderGroupByUsage() {
        return mOrderGroupByUsage;
    }

    public SQLType getColumnUsage() {
        return mColumnUsage;
    }

    public SQLType getTableUsage() {
        return mTableUsage;
    }

    public SQLType getViewUsage() {
        return mViewUsage;
    }

    public SQLType getProcUsage() {
        return mProcUsage;
    }

    public SQLType getFunUsage() {
        return mFunUsage;
    }

    public SQLType getEventUsage() {
        return mEventUsage;
    }

    public SQLType getTriggerUsage() {
        return mTriggerUsage;
    }

    public SQLType getIndexUsage() {
        return mIndexUsage;
    }

    public SQLType getDBUsage() {
        return mDBUsage;
    }

    public SQLType getServerUsage() {
        return mServerUsage;
    }

    public SQLType getTableSpaceUsage() {
        return mTableSpaceUsage;
    }

    public SQLType getTableUsageLower() {
        return mTableUsageLower;
    }

    public SQLType getSQLUsageLower() {
        return mSQLUsageLower;
    }

    public static class GitFileAnalysisResultDtoBuilder {
        private String mCommitDate = null;
        private ApiType mApiType = ApiType.NONE;
        private String mProjectName;
        private String mFileUrl;
        private SQLType mSQLUsage = SQLType.NONE;
        private SQLType mSQLUsageLower = SQLType.NONE;
        private SQLType mLikeUsage = SQLType.NONE;
        private SQLType mOrderGroupByUsage = SQLType.NONE;
        private SQLType mColumnUsage = SQLType.NONE;
        private SQLType mTableUsage = SQLType.NONE;
        private SQLType mTableUsageLower = SQLType.NONE;
        private SQLType mViewUsage = SQLType.NONE;
        private SQLType mProcUsage = SQLType.NONE;
        private SQLType mFunUsage = SQLType.NONE;
        private SQLType mEventUsage = SQLType.NONE;
        private SQLType mTriggerUsage = SQLType.NONE;
        private SQLType mIndexUsage = SQLType.NONE;
        private SQLType mDBUsage = SQLType.NONE;
        private SQLType mServerUsage = SQLType.NONE;
        private SQLType mTableSpaceUsage = SQLType.NONE;

        public GitFileAnalysisResultDtoBuilder setSQLUsageLower(SQLType SQLUsageLower) {
            mSQLUsageLower = SQLUsageLower;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setTableUsageLower(SQLType tableUsageLower) {
            mTableUsageLower = tableUsageLower;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setCommitDate(String commitDate) {
            mCommitDate = commitDate;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setApiType(ApiType apiType) {
            mApiType = apiType;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setProjectName(String projectName) {
            mProjectName = projectName;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setFileUrl(String fileUrl) {
            mFileUrl = fileUrl;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setSQLUsage(SQLType SQLUsage) {
            mSQLUsage = SQLUsage;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setLikeUsage(SQLType likeUsage) {
            mLikeUsage = likeUsage;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setOrderGroupByUsage(SQLType orderGroupByUsage) {
            mOrderGroupByUsage = orderGroupByUsage;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setColumnUsage(SQLType columnUsage) {
            mColumnUsage = columnUsage;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setTableUsage(SQLType tableUsage) {
            mTableUsage = tableUsage;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setViewUsage(SQLType viewUsage) {
            mViewUsage = viewUsage;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setProcUsage(SQLType procUsage) {
            mProcUsage = procUsage;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setFunUsage(SQLType funUsage) {
            mFunUsage = funUsage;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setEventUsage(SQLType eventUsage) {
            mEventUsage = eventUsage;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setTriggerUsage(SQLType triggerUsage) {
            mTriggerUsage = triggerUsage;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setIndexUsage(SQLType indexUsage) {
            mIndexUsage = indexUsage;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setDBUsage(SQLType DBUsage) {
            mDBUsage = DBUsage;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setServerUsage(SQLType serverUsage) {
            mServerUsage = serverUsage;
            return this;
        }

        public GitFileAnalysisResultDtoBuilder setTableSpaceUsage(SQLType tableSpaceUsage) {
            mTableSpaceUsage = tableSpaceUsage;
            return this;
        }

        public GitFileAnalysisResultDto createGitFileAnalysisResultDto() {
            return new GitFileAnalysisResultDto(mCommitDate, mApiType, mProjectName, mFileUrl, mSQLUsage, mSQLUsageLower,  mLikeUsage,
                    mOrderGroupByUsage, mColumnUsage, mTableUsage, mTableUsageLower, mViewUsage, mProcUsage, mFunUsage, mEventUsage,
                    mTriggerUsage, mIndexUsage, mDBUsage, mServerUsage, mTableSpaceUsage);
        }
    }
}
