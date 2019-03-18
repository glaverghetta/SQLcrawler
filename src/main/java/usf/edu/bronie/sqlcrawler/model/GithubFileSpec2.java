package usf.edu.bronie.sqlcrawler.model;

public class GithubFileSpec2 {

    private String mProjectName;

    private int mStarCount;

    private int mWatchCount;

    private int mForkCount;

    private int mCommitDate;

    private int mForkedFrom;

    private int mTotalCommit;

    private int mTotalBranch;

    private int mTotalContribution;

    private int mTotalRelease;

    public GithubFileSpec2(String projectName, int starCount, int watchCount,
                           int forkCount, int commitDate, int forkedFrom, int totalCommit,
                           int totalBranch, int totalContribution, int totalRelease) {
        mProjectName = projectName;
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

    public String getProjectName() {
        return mProjectName;
    }

    public int getStarCount() {
        return mStarCount;
    }

    public int getWatchCount() {
        return mWatchCount;
    }

    public int getForkCount() {
        return mForkCount;
    }

    public int getCommitDate() {
        return mCommitDate;
    }

    public int getForkedFrom() {
        return mForkedFrom;
    }

    public int getTotalCommit() {
        return mTotalCommit;
    }

    public int getTotalBranch() {
        return mTotalBranch;
    }

    public int getTotalContribution() {
        return mTotalContribution;
    }

    public int getTotalRelease() {
        return mTotalRelease;
    }

    public static class GithubFileSpec2Builder {
        private String mProjectName;
        private int mStarCount;
        private int mWatchCount;
        private int mForkCount;
        private int mCommitDate;
        private int mForkedFrom;
        private int mTotalCommit;
        private int mTotalBranch;
        private int mTotalContribution;
        private int mTotalRelease;

        public GithubFileSpec2Builder setProjectName(String projectName) {
            mProjectName = projectName;
            return this;
        }

        public GithubFileSpec2Builder setStarCount(int starCount) {
            mStarCount = starCount;
            return this;
        }

        public GithubFileSpec2Builder setWatchCount(int watchCount) {
            mWatchCount = watchCount;
            return this;
        }

        public GithubFileSpec2Builder setForkCount(int forkCount) {
            mForkCount = forkCount;
            return this;
        }

        public GithubFileSpec2Builder setCommitDate(int commitDate) {
            mCommitDate = commitDate;
            return this;
        }

        public GithubFileSpec2Builder setForkedFrom(int forkedFrom) {
            mForkedFrom = forkedFrom;
            return this;
        }

        public GithubFileSpec2Builder setTotalCommit(int totalCommit) {
            mTotalCommit = totalCommit;
            return this;
        }

        public GithubFileSpec2Builder setTotalBranch(int totalBranch) {
            mTotalBranch = totalBranch;
            return this;
        }

        public GithubFileSpec2Builder setTotalContribution(int totalContribution) {
            mTotalContribution = totalContribution;
            return this;
        }

        public GithubFileSpec2Builder setTotalRelease(int totalRelease) {
            mTotalRelease = totalRelease;
            return this;
        }

        public GithubFileSpec2 createGithubFileSpec2() {
            return new GithubFileSpec2(mProjectName, mStarCount, mWatchCount, mForkCount, mCommitDate, mForkedFrom, mTotalCommit, mTotalBranch, mTotalContribution, mTotalRelease);
        }
    }
}
