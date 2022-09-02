package usf.edu.bronie.sqlcrawler;

import usf.edu.bronie.sqlcrawler.analyze.CodeAnalyzer;
import usf.edu.bronie.sqlcrawler.analyze.GroupOrderByCodeAnalyzer;
import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.crawler.GithubCrawler;
import usf.edu.bronie.sqlcrawler.analyze.CodeStatistics;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.model.SearchData;
import usf.edu.bronie.sqlcrawler.provider.ExcelProvider;
import usf.edu.bronie.sqlcrawler.provider.GithubProvider;
import usf.edu.bronie.sqlcrawler.provider.SourceCodeProvider;
import usf.edu.bronie.sqlcrawler.provider.SearchCodeProvider;
import usf.edu.bronie.sqlcrawler.manager.CodeAnalysisManager;
import usf.edu.bronie.sqlcrawler.model.Analysis;
import usf.edu.bronie.sqlcrawler.model.File;
import usf.edu.bronie.sqlcrawler.model.GithubFileSpec;
import usf.edu.bronie.sqlcrawler.model.Project;

import usf.edu.bronie.sqlcrawler.model.SearchCode.SearchCodeResult;
import usf.edu.bronie.sqlcrawler.model.SearchData;

import usf.edu.bronie.sqlcrawler.utils.UrlUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;


import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
@Command(name = "ISOCodeResolve", subcommands = { CommandLine.HelpCommand.class, Pull.class, Analyze.class,
		Statistics.class , Optimize.class, TestDummyFile.class}, description = "Tool for analyzing SQLIDIA vulnerabilities")
public class CrawlerMain {
	public static void main(String[] args) {
		CommandLine cmd = new CommandLine(new CrawlerMain());
		if (args.length == 0) {
			cmd.usage(System.out);
		} else {
			cmd.execute(args);
		}
	}
}

// Provider command
// Runs the provider, given the number of pages
@Command(name = "pull", description = "Pulls [number] of pages from Github")
class Pull implements Runnable {
	@Parameters(paramLabel = "[number]", description = "number of pages to pull from Github")
    int numberOfFiles;
	
	@Override
	public void run() {
		System.out.println("Running provider");
		System.out.println("Pulling " + numberOfFiles + " files");
	}
}

// Analyzer command
// Runs the analyzer, given either [all] or [new]
@Command(name = "analyze", description = "Analyze either [all] or [new] entries from the database")
class Analyze implements Runnable {
	@Override
	public void run() {
		// Runs when no sub-command is provided
		// Default behavior to running leftover
		analyzeNew();
	}

	@Command(name = "all", description = "Analyze all files in the database")
	void analyzeAll() {
		System.out.println("Add all analysis code here");
	}

	@Command(name = "new", description = "Analyze any new, non-analyzed files in the database")
	void analyzeNew() {
		System.out.println("Add new analysis code here");
	}

}

// Statistics command
@Command(name = "stats", description = "Provide statistics for either [all] or [new] entries from the database")
class Statistics implements Runnable {

	@Override
	public void run() {
		// Runs when no sub-command is provided
		// Default behavior to running leftover
		System.out.println(RegexConstants.STRING_LITERAL_CONCAT_WITH_GROUP_ORDER_BY);

		statisticsNew();
	}

	@Command(name = "all", description = "Analyze all files in the database")
	void statisticsAll() {
		System.out.println("Add all statistics code here");
	}

	@Command(name = "new", description = "Analyze any new, non-analyzed files in the database")
	void statisticsNew() {
		System.out.println("Add new statistics code here");
	}
}

// Optimize command
@Command(name = "optimize", description = "Runs all 3 functionalities")
class Optimize implements Runnable {
	@Parameters(paramLabel = "[number]", description = "number of pages to pull from Github")
    int numberOfFiles;
	
	@Override
	public void run() {
		System.out.println("Running optimize mode");
	}
}

//Optimize command
@Command(name = "test", description = "Tests a dummy file using the analyzer")
class TestDummyFile implements Runnable {
	
	@Override
	public void run() {
		System.out.println("Running test dummy file option");
		System.out.println("The test file will be named dummy.java in the same directory as main");
		// Create a fake file
        // Same as above, this won't create a new file on subsequent runs
        // unless you update the file name or path, which might be useful (i.e., name the
        // file "TriggerTest")
        File dummyFile = new File("dummyFile", "dummyPath", "https://github.com/dummy/dummyRepo/raw/not_a_real_raw_url",
                "haaaaaash", "haaaaash again");
        
        dummyFile.save(); //Creates a project as well

        // Now load the code directly from a file instead of pulling from a url.
        // Note that the getCode function will visit the url if not already set,
        // so by setting it now we bypass that.
        // I placed my file in the same folder as main
        Path filePath = Path.of("src/main/java/usf/edu/bronie/sqlcrawler/dummy.java");

        try {
            String code = Files.readString(filePath, StandardCharsets.US_ASCII);
            dummyFile.setCode(code);
        } catch (IOException e) {
            System.out.println("Error reading file");
            System.out.println(e);
            System.exit(-1);
        }

        //Now for the actual analysis!
        CodeAnalysisManager cam = new CodeAnalysisManager();
        Analysis a = cam.processFile(dummyFile);
        a.save();
        System.out.println("Successfully analyzed the dummy file");
        return;
	}
}


/*
public class CrawlerMain {

    public static void main(String[] args) {
        
        //Here is a SQL query to retrieve the results from the DB
        //  SELECT f.filename, a.* FROM crawler.analyses a LEFT JOIN crawler.projects p 
        //  ON p.id = a.project LEFT JOIN crawler.files f ON f.id = a.file WHERE p.name="dummy/dummyRepo";

        // Kevin's code testing the Github provider

        // CodeAnalysisManager cam = new CodeAnalysisManager();
        // GithubProvider scp = new GithubProvider();
        // scp.pollData(0, 5);

        // int updated = 0;
        // int total = 0;

        // while (scp.hasNext()) {
        // total++;
        // File result = scp.receiveNextData();
        // if(result.save()) updated++;
        // Analysis a = cam.processFile(result);
        // a.save();
        // }
        // System.out.println(String.format("Scanned and analyzed %d files, added %d new
        // files", total, updated));

        // TODO: Add the CLI for main so that everyone can work without commenting other
        // people's stuff out

        /*
         * String a = UrlUtils.convertRawGithubToRepo(
         * "https://raw.githubusercontent.com/Ktrio3/SQLcrawler/cfc9c02c0ea846148299a15570a4b06b10fcd3f8/src/main/java/usf/edu/bronie/sqlcrawler/CrawlerMain.java"
         * );
         * 
         * System.out.println(a);
         * 
         * Project project = new Project("Connected_ITProjektSS18",
         * "https://github.com/Philipp-Mueller/Connected_ITProjektSS18");
         * 
         * project.save();
         * 
         * System.out.println(Project.idFromRepo(
         * "https://github.com/Philipp-Mueller/Connected_ITProjektSS18"));
         */

        // String fileName = "urls.txt";
        // System.out.println("Providing urls from a file: " + fileName);

        // // Provider
        // ExcelProvider excelProvider = new ExcelProvider();
        // excelProvider.setFile(fileName);
        // excelProvider.pollData();
        // excelProvider.printCurrentArray();

        // // Crawler

        // //while(excelProvider.hasNext()) {
        // SearchData searchData = excelProvider.receiveNextData();
        // GithubCrawler gc = new GithubCrawler();
        // GithubFileSpec gh = gc.getFileSpecByUrl(searchData.getRawUrl());
        // gh.printFileSpecData();
        // //}

        // CodeAnalyzer codeAnalyzer = new GroupOrderByCodeAnalyzer();

        // try {
        // int min = Integer.valueOf(0);
        // int max = Integer.valueOf(10);
        // new CodeAnalysisManager().analyzeCode(min, max);
        // } catch (SQLException e) {
        // e.printStackTrace();
        // }

    //}
//}}