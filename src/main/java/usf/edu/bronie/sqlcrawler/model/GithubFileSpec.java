package usf.edu.bronie.sqlcrawler.model;

public class GithubFileSpec {

    private String mStarCount;

    private String mWatchCount;

    private String mForkCount;

    private String mCommitDate;

    private String mForkedFrom;

    private String mTotalCommit;

    private String mTotalBranch;

    private String mTotalContribution;

    private String mTotalRelease;

    private GithubFileSpec(String starCount, String watchCount, String forkCount, String commitDate,
                          String forkedFrom, String totalCommit, String totalBranch,
                          String totalContribution, String totalRelease) {
        mStarCount = starCount;
        mWatchCount = watchCount;
        mForkCount = forkCount;
        mCommitDate = commitDate;
        mForkedFrom = forkedFrom;
        mTotalCommit = totalCommit;
        mTotalBranch = totalBranch;
        mTotalContribution = totalContribution;
        mTotalRelease = totalRelease;
    }

    public String getStarCount() {
        return mStarCount;
    }

    public String getWatchCount() {
        return mWatchCount;
    }

    public String getForkCount() {
        return mForkCount;
    }

    public String getCommitDate() {
        return mCommitDate;
    }

    public String getForkedFrom() {
        return mForkedFrom;
    }

    public String getTotalCommit() {
        return mTotalCommit;
    }

    public String getTotalBranch() {
        return mTotalBranch;
    }

    public String getTotalContribution() {
        return mTotalContribution;
    }

    public String getTotalRelease() {
        return mTotalRelease;
    }

    public static class GithubFileSpecBuilder {
        private String mStarCount;
        private String mWatchCount;
        private String mForkCount;
        private String mCommitDate;
        private String mForkedFrom;
        private String mTotalCommit;
        private String mTotalBranch;
        private String mTotalContribution;
        private String mTotalRelease;

        public GithubFileSpecBuilder setStarCount(String starCount) {
            mStarCount = starCount;
            return this;
        }

        public GithubFileSpecBuilder setWatchCount(String watchCount) {
            mWatchCount = watchCount;
            return this;
        }

        public GithubFileSpecBuilder setForkCount(String forkCount) {
            mForkCount = forkCount;
            return this;
        }

        public GithubFileSpecBuilder setCommitDate(String commitDate) {
            mCommitDate = commitDate;
            return this;
        }

        public GithubFileSpecBuilder setForkedFrom(String forkedFrom) {
            mForkedFrom = forkedFrom;
            return this;
        }

        public GithubFileSpecBuilder setTotalCommit(String totalCommit) {
            mTotalCommit = totalCommit;
            return this;
        }

        public GithubFileSpecBuilder setTotalBranch(String totalBranch) {
            mTotalBranch = totalBranch;
            return this;
        }

        public GithubFileSpecBuilder setTotalContribution(String totalContribution) {
            mTotalContribution = totalContribution;
            return this;
        }

        public GithubFileSpecBuilder setTotalRelease(String totalRelease) {
            mTotalRelease = totalRelease;
            return this;
        }

        public GithubFileSpec createGithubFileSpec() {
            return new GithubFileSpec(mStarCount, mWatchCount, mForkCount, mCommitDate, mForkedFrom,
                    mTotalCommit, mTotalBranch, mTotalContribution, mTotalRelease);
        }
    }
}
