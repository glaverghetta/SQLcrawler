package usf.edu.bronie.sqlcrawler;

import usf.edu.bronie.sqlcrawler.analyze.CodeAnalyzer;
import usf.edu.bronie.sqlcrawler.analyze.GroupOrderByCodeAnalyzer;
import usf.edu.bronie.sqlcrawler.analyze.CodeStatistics;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.model.SearchData;
import usf.edu.bronie.sqlcrawler.provider.GithubProvider;
import usf.edu.bronie.sqlcrawler.provider.SourceCodeProvider;
import usf.edu.bronie.sqlcrawler.provider.SearchCodeProvider;
import usf.edu.bronie.sqlcrawler.manager.CodeAnalysisManager;

import usf.edu.bronie.sqlcrawler.model.Project;

import usf.edu.bronie.sqlcrawler.model.SearchCode.SearchCodeResult;
import usf.edu.bronie.sqlcrawler.model.SearchData;

import usf.edu.bronie.sqlcrawler.utils.UrlUtils;

import java.sql.SQLException;

public class CrawlerMain {

    public static void main(String[] args) {
    	System.out.println("Testing from Bianca");

        String a = UrlUtils.convertRawGithubToRepo("https://raw.githubusercontent.com/Ktrio3/SQLcrawler/cfc9c02c0ea846148299a15570a4b06b10fcd3f8/src/main/java/usf/edu/bronie/sqlcrawler/CrawlerMain.java");

        System.out.println(a);

        Project project = new Project("Connected_ITProjektSS18", "https://github.com/Philipp-Mueller/Connected_ITProjektSS18");

        project.save();

        System.out.println(Project.idFromRepo("https://github.com/Philipp-Mueller/Connected_ITProjektSS18"));

        // CodeAnalyzer codeAnalyzer = new GroupOrderByCodeAnalyzer();
        // SourceCodeProvider scp = new SearchCodeProvider();
        // // SourceCodeProvider scp = new GithubProvider();
        // scp.pollData();

        // int i = 0;

        // while (scp.hasNext()) {
        //     System.out.print("\rAnalyzing -- file number: " + ++i);

        //     SearchData searchData = scp.receiveNextData();
        // }

        // try {
        //     int min = Integer.valueOf(0);
        //     int max = Integer.valueOf(10);
        //     new CodeAnalysisManager().analyzeCode(min, max);
        // } catch (SQLException e) {
        //     e.printStackTrace();
        // }

    }
}