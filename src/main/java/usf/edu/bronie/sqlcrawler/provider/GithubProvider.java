package usf.edu.bronie.sqlcrawler.provider;

import com.google.gson.Gson;

import usf.edu.bronie.sqlcrawler.constants.CredentialConstants;
import usf.edu.bronie.sqlcrawler.constants.UrlConstants;
import usf.edu.bronie.sqlcrawler.io.GithubAPI;
import usf.edu.bronie.sqlcrawler.io.HttpConnection;
import usf.edu.bronie.sqlcrawler.io.GithubAPI.RateLimitException;
import usf.edu.bronie.sqlcrawler.io.GithubAPI.SecondaryLimitException;
import usf.edu.bronie.sqlcrawler.model.File;
import usf.edu.bronie.sqlcrawler.model.Github.SearchCode;
import usf.edu.bronie.sqlcrawler.model.Github.Item;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Using searchcode.com, the provider finds files and projects to be analyzed.
 * 
 * TODO: need to add params to modify the implementation.  For example, the language, sources, and page numbers
 */

public class GithubProvider {

    private static final Logger log = LoggerFactory.getLogger(GithubProvider.class);

    /** 
     * Queue containing all of the results the provider has found. Populated using
     * {@link #pollData() pollData} and accessed using {@link #receiveNextData() receiveNextData}.
    */
    private Queue<File> mQueue = new LinkedList();
    private GithubAPI api = new GithubAPI();

    public boolean hasNext() {
        return !mQueue.isEmpty();
    }

    //TODO: We should add a configure method so we can set things like the language, pages, etc.
    //I also don't like that this prints straight to the console.  Should look into some proper logging, although 
    // that may be overkill
    // TODO: Need to add support for checking limits
    // public void pollData() {
    //     for (int i = 0; i < UrlConstants.GITHUB_DEFAULT_MAX_PAGE; i++) {
    //         System.out.print("\rFetching data -- file number: " + i);

    //         addAllUrlsByPage(i);
    //     }

    //     System.out.println(" ");
    //     System.out.println(" -------------------------------------- ");
    //     System.out.println("Total number of files to analyze: " + mQueue.size());
    //     System.out.println(" -------------------------------------- ");
    // }

    public void pollData(int start, int end, String language) {
        for (int i = start; i < end; i++) {
            //addAllUrlsByPage(i, language);
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
    public void addAllUrlsByPage(int pageNumber, String language) throws SecondaryLimitException, RateLimitException {

        String page = this.api.search(pageNumber, language);
        Gson gson = new Gson();
        SearchCode scr = gson.fromJson(page, SearchCode.class);
        
        if(scr == null){
            //Unknown error, print out the response
            log.error("Unknown response from Github:\n {}", page);
            System.exit(-1);
        }

        List<Item> list = scr.getItems();

        // TODO: Currently, this just adds the raw url directly to the code on Searchcode.  We want more information than that though.
        for (Item r: list) {
            mQueue.add(new File(r.getName(), r.getPath(), r.getRawUrl(), r.getSha(), r.getCommit()));
        }
    }
}
