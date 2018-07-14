package usf.edu.bronie.sqlcrawler.provider;

import com.google.gson.Gson;
import usf.edu.bronie.sqlcrawler.constants.UrlConstants;
import usf.edu.bronie.sqlcrawler.io.HttpConnection;
import usf.edu.bronie.sqlcrawler.model.GithubData;
import usf.edu.bronie.sqlcrawler.model.RootGitObject;
import usf.edu.bronie.sqlcrawler.model.SearchData;

import java.util.ArrayList;

public class GithubProvider implements SourceCodeProvider {

    private ArrayList<GithubData> mGithubData;
    private int mIndex = 0;

    public boolean hasNext() {
        return mIndex < mGithubData.size();
    }

    public void pollData() {
        System.out.print("\rFetching data");
        String url = UrlConstants.GDRIVE_URL + UrlConstants.GDRIVE_PAGE;
        String s = HttpConnection.get(url);
        RootGitObject rootGitObject = new Gson().fromJson(s, RootGitObject.class);
        mGithubData = rootGitObject.getGithubData();

        System.out.println(" ");
        System.out.println(" -------------------------------------- ");
        System.out.println("Total number of files to analyze: " + mGithubData.size());
        System.out.println(" -------------------------------------- ");
    }

    public SearchData receiveNextData() {
        GithubData githubData = mGithubData.get(mIndex);
        String encode = githubData.getUrl().replaceAll(" ", "%20");
        String code = HttpConnection.get(encode);
        SearchData searchData = new SearchData(githubData.getUrl(), githubData.getName());
        searchData.setCode(code);
        mIndex++;
        return searchData;
    }
}
