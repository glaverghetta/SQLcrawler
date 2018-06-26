package usf.edu.bronie.sqlcrawler.provider;

import usf.edu.bronie.sqlcrawler.model.SearchData;

public interface SourceCodeProvider {

    boolean hasNext();

    void pollData();

    SearchData receiveNextData();
}
