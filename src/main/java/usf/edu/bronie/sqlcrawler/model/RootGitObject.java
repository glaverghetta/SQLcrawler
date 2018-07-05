package usf.edu.bronie.sqlcrawler.model;

import java.util.ArrayList;

public class RootGitObject {
    private ArrayList<GithubData> GithubData;

    public ArrayList<GithubData> getGithubData() {
        return this.GithubData;
    }

    public void setGithubData(ArrayList<GithubData> GithubData) {
        this.GithubData = GithubData;
    }
}
