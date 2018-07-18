package usf.edu.bronie.sqlcrawler.model;

public class GithubFileSpec {

    private String mStarCount;

    private String mWatchCount;

    private String mForkCount;

    private String mCommitDate;

    private GithubFileSpec(String starCount, String watchCount, String forkCount, String commitDate) {
        mStarCount = starCount;
        mWatchCount = watchCount;
        mForkCount = forkCount;
        mCommitDate = commitDate;
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

    public static class GithubFileSpecBuilder {
        private String mStarCount;
        private String mWatchCount;
        private String mForkCount;
        private String mCommitDate;

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

        public GithubFileSpec createGithubFileSpec() {
            return new GithubFileSpec(mStarCount, mWatchCount, mForkCount, mCommitDate);
        }
    }
}
