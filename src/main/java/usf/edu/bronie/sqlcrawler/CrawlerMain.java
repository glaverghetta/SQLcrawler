package usf.edu.bronie.sqlcrawler;

import usf.edu.bronie.sqlcrawler.analyze.CodeAnalyzer;
import usf.edu.bronie.sqlcrawler.analyze.CodeStatistics;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.model.SearchData;
import usf.edu.bronie.sqlcrawler.provider.SearchCodeProvider;
import usf.edu.bronie.sqlcrawler.provider.SourceCodeProvider;

public class CrawlerMain {
    public static void main(String[] args) {

        CodeStatistics codeStatistics = new CodeStatistics();
        CodeAnalyzer codeAnalyzer = new CodeAnalyzer();
        SourceCodeProvider scp = new SearchCodeProvider();
        scp.pollData();

        int i = 0;

        while (scp.hasNext()) {
            System.out.print("\rAnalyzing -- file number: " + ++i);

            SearchData searchData = scp.receiveNextData();
            if (searchData != null) {
                SQLType sqlType = codeAnalyzer.analyzeCode(searchData.getCode());
                codeStatistics.collectData(sqlType, searchData.getProjectName());
            }
        }

        codeStatistics.printResults();
    }
}
