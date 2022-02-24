package usf.edu.bronie.sqlcrawler.model;

public class GitDataDTO {

    private String mRepoName;

    private String mRef;

    private String mPath;

    public GitDataDTO(String repoName, String ref, String path) {
        mRepoName = repoName;
        mRef = ref;
        mPath = path;
    }

    public String getRepoName() {
        return mRepoName;
    }

    public String getRef() {
        return mRef;
    }

    public String getPath() {
        return mPath;
    }
}
