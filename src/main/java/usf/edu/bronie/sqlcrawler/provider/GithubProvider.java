package usf.edu.bronie.sqlcrawler.provider;

import com.google.api.client.auth.oauth2.Credential;
import com.google.gson.Gson;

import usf.edu.bronie.sqlcrawler.constants.CredentialConstants;
import usf.edu.bronie.sqlcrawler.constants.UrlConstants;
import usf.edu.bronie.sqlcrawler.io.HttpConnection;
import usf.edu.bronie.sqlcrawler.model.File;
import usf.edu.bronie.sqlcrawler.model.Github.SearchCode;
import usf.edu.bronie.sqlcrawler.model.Github.Item;
import usf.edu.bronie.sqlcrawler.utils.UrlUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Map;

/**
 * Using searchcode.com, the provider finds files and projects to be analyzed.
 * 
 * TODO: need to add params to modify the implementation.  For example, the language, sources, and page numbers
 */

public class GithubProvider {

    /** 
     * Queue containing all of the results the provider has found. Populated using
     * {@link #pollData() pollData} and accessed using {@link #receiveNextData() receiveNextData}.
    */
    private Queue<File> mQueue = new LinkedList();

    public boolean hasNext() {
        return !mQueue.isEmpty();
    }

    //TODO: We should add a configure method so we can set things like the language, pages, etc.
    //I also don't like that this prints straight to the console.  Should look into some proper logging, although 
    // that may be overkill
    // TODO: Need to add support for checking limits
    public void pollData() {
        for (int i = 0; i < UrlConstants.GITHUB_DEFAULT_MAX_PAGE; i++) {
            System.out.print("\rFetching data -- file number: " + i);

            addAllUrlsByPage(i);
        }

        System.out.println(" ");
        System.out.println(" -------------------------------------- ");
        System.out.println("Total number of files to analyze: " + mQueue.size());
        System.out.println(" -------------------------------------- ");
    }

    public void pollData(int start, int end) {
        for (int i = start; i < end; i++) {
            addAllUrlsByPage(i);
        }
    }

    public File receiveNextData() {
        if (mQueue.isEmpty())
            return null;

        File poll = (File) mQueue.poll();

        return poll;
    }

    /**
     * Makes a call to the searchcode API to retrieve the query results. Searchcode 
     * results are split into pages. The provided page is queried and the results
     * are stored into a queue to be accessed using {@link #receiveNextData() receiveNextData}.
     * 
     * @param pageNumber The page number to query
     */
    private void addAllUrlsByPage(int pageNumber) {
        String url = String.format(UrlConstants.GITHUB_SEARCH_URL, "executeQuery+language:java", 100, pageNumber);
        Map<String, String> headers = Map.of(
            "Accept", "application/vnd.github+json",
            "Authorization", "token " + CredentialConstants.GITHUB_TOKEN
        );
        String s = HttpConnection.get(url, headers);
        
        Gson gson = new Gson();
        SearchCode scr = gson.fromJson(s, SearchCode.class);

        List<Item> list = scr.getItems();

        // TODO: Currently, this just adds the raw url directly to the code on Searchcode.  We want more information than that though.
        for (Item r: list) {
            mQueue.add(new File(r.getName(), r.getPath(), r.getRawUrl(), r.getSha(), r.getCommit()));
        }
    }
}
