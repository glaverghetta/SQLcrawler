package usf.edu.bronie.sqlcrawler.provider;

import com.google.gson.Gson;
import usf.edu.bronie.sqlcrawler.constants.UrlConstants;
import usf.edu.bronie.sqlcrawler.io.HttpConnection;
import usf.edu.bronie.sqlcrawler.model.Results;
import usf.edu.bronie.sqlcrawler.model.SearchCodeResult;
import usf.edu.bronie.sqlcrawler.model.SearchData;
import usf.edu.bronie.sqlcrawler.utils.UrlUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SearchCodeProvider implements SourceCodeProvider{

    private Queue mQueue = new LinkedList();

    public boolean hasNext() {
        return !mQueue.isEmpty();
    }

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

    private void addAllUrlsByPage(int pageNumber) {
        String s = HttpConnection.get(UrlConstants.SEARCHCODE_URL + pageNumber);
        Gson gson = new Gson();
        SearchCodeResult scr = gson.fromJson(s, SearchCodeResult.class);

        List<Results> list = scr.getResults();

        for (Results r: list) {
            String rawDataUrl = UrlUtils.convertSearchCodeToRawDataUrl(r.getUrl());
            mQueue.add(new SearchData(rawDataUrl, r.getName()));
        }
    }
}
