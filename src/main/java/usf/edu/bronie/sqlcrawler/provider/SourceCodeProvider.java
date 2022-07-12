package usf.edu.bronie.sqlcrawler.provider;

import usf.edu.bronie.sqlcrawler.model.SearchData;

public interface SourceCodeProvider {

    /**
     * Checks if there are more results to retrieve using {@link #receiveNextData() receiveNextData}.
     * 
     * @return True if there is another result, false if no results  
     */
    boolean hasNext();

    /**
     * Performs the search for interesting code snippets to analyze, storing the  
     * results in a queue.
     */
    void pollData();

    /**
     * Returns the next code result found by the provider.
     * 
     * @return The resulting {@link usf.edu.bronie.sqlcrawler.model.SearchData search data} or
     *      null if no code results remain
     */
    SearchData receiveNextData();
}
