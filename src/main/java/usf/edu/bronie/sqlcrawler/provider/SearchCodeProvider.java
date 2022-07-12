package usf.edu.bronie.sqlcrawler.provider;

import com.google.gson.Gson;
import usf.edu.bronie.sqlcrawler.constants.UrlConstants;
import usf.edu.bronie.sqlcrawler.io.HttpConnection;
import usf.edu.bronie.sqlcrawler.model.SearchData;
import usf.edu.bronie.sqlcrawler.model.SearchCode.Results;
import usf.edu.bronie.sqlcrawler.model.SearchCode.SearchCodeResult;
import usf.edu.bronie.sqlcrawler.utils.UrlUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Using searchcode.com, the provider finds files and projects to be analyzed.
 * 
 * TODO: need to add params to modify the implementation.  For example, the language, sources, and page numbers
 */

public class SearchCodeProvider implements SourceCodeProvider{

    /** 
     * Queue containing all of the results the provider has found. Populated using
     * {@link #pollData() pollData} and accessed using {@link #receiveNextData() receiveNextData}.
    */
    private Queue mQueue = new LinkedList();

    public boolean hasNext() {
        return !mQueue.isEmpty();
    }

    //TODO: We should add a configure method so we can set things like the language, pages, etc.
    //I also don't like that this prints straight to the console.  Should look into some proper logging, although 
    // that may be overkill
    public void pollData() {
        for (int i = 0; i < UrlConstants.SEARCHCODE_MAX_PAGE; i++) {
            System.out.print("\rFetching data -- file number: " + i);

            addAllUrlsByPage(i);
        }

        System.out.println(" ");
        System.out.println(" -------------------------------------- ");
        System.out.println("Total number of files to analyze: " + mQueue.size());
        System.out.println(" -------------------------------------- ");
    }

    public SearchData receiveNextData() {
        if (mQueue.isEmpty())
            return null;

        SearchData poll = (SearchData) mQueue.poll();
        String s = HttpConnection.get(poll.getRawUrl());
        poll.setCode(s);

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
        String s = HttpConnection.get(UrlConstants.SEARCHCODE_URL + pageNumber);
        System.out.println(s);
        Gson gson = new Gson();
        SearchCodeResult scr = gson.fromJson(s, SearchCodeResult.class);

        List<Results> list = scr.getResults();

        // TODO: Currently, this just adds the raw url directly to the code on Searchcode.  We want more information than that though.
        for (Results r: list) {
            String rawDataUrl = UrlUtils.convertSearchCodeToRawDataUrl(r.getUrl());
            mQueue.add(new SearchData(rawDataUrl, r.getName()));
        }
    }
}
