package usf.edu.bronie.sqlcrawler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters; //K: Is ISOCodeResolve the app name (I assume from an example)? Should this be SQLCrawler?
import usf.edu.bronie.sqlcrawler.constants.RegexConstants.Languages;
import usf.edu.bronie.sqlcrawler.io.GithubAPI;
import usf.edu.bronie.sqlcrawler.io.GithubAPI.PageLimitException;
import usf.edu.bronie.sqlcrawler.manager.CodeAnalysisManager;
import usf.edu.bronie.sqlcrawler.model.Analysis;
import usf.edu.bronie.sqlcrawler.model.File;

@Command(name = "ISOCodeResolve", subcommands = { CommandLine.HelpCommand.class, Pull.class, Analyze.class, Kevin.class,
        Statistics.class, Optimize.class,
        TestDummyFile.class }, description = "Tool for analyzing SQLIDIA vulnerabilities")
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

@Command(name = "kevin", description = "Do what Kevin says :)")
class Kevin implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(Kevin.class);

    @Override
    public void run() {
        // Kevin's code testing the Github provider

        CodeAnalysisManager cam = new CodeAnalysisManager();
        GithubAPI gh = new GithubAPI(1, 1000,  Languages.JAVA);

        int updated = 0;
        int total = 0;
        int lastTotal = 0;

        while(gh.isNextPage()){
            Queue<File> results = null;

            try{
                results = gh.searchSleep();
            }catch(PageLimitException e){
                log.error("", e);
                System.exit(-1);
            }

            while (!results.isEmpty()) {
                total++;
                File result = results.poll();
                if (result.save())
                    updated++;
                Analysis a = cam.processFile(result);
                a.save();
            }
            log.debug("Finished scanning page {} (page had {} results, {} total)", gh.lastPagePulled(), total - lastTotal, total);
            lastTotal = total;
        }

        log.info("Scanned and analyzed {} files, added {} new files", total, updated);
    }
}

// Provider command
// Runs the provider, given the number of pages
@Command(name = "pull", description = "Pulls [number] of pages from Github")
class Pull implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(Pull.class);

    @Parameters(paramLabel = "[number]", description = "number of pages to pull from Github")
    int numberOfFiles;

    @Override
    public void run() {
        log.info("Running provider");
        log.info("Pulling {} files", numberOfFiles);
    }
}

// Analyzer command
// Runs the analyzer, given either [all] or [new]
@Command(name = "analyze", description = "Analyze either [all] or [new] entries from the database")
class Analyze implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(Analyze.class);

    @Override
    public void run() {
        // Runs when no sub-command is provided
        // Default behavior to running leftover
        analyzeNew();
    }

    @Command(name = "all", description = "Analyze all files in the database")
    void analyzeAll() {
        log.info("Add all analysis code here");
    }

    @Command(name = "new", description = "Analyze any new, non-analyzed files in the database")
    void analyzeNew() {
        log.info("Add new analysis code here");
    }

}

// Statistics command
@Command(name = "stats", description = "Provide statistics for either [all] or [new] entries from the database")
class Statistics implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(Statistics.class);

    @Override
    public void run() {
        // Runs when no sub-command is provided
        // Default behavior to running leftover

        statisticsNew();
    }

    @Command(name = "all", description = "Analyze all files in the database")
    void statisticsAll() {
        log.info("Add all statistics code here");
    }

    @Command(name = "new", description = "Analyze any new, non-analyzed files in the database")
    void statisticsNew() {
        log.info("Add new statistics code here");
    }
}

// Optimize command
@Command(name = "optimize", description = "Runs all 3 functionalities")
class Optimize implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(Optimize.class);
    
    @Parameters(paramLabel = "[number]", description = "number of pages to pull from Github")
    int numberOfFiles;

    @Override
    public void run() {
        log.info("Running optimize mode");
    }
}

// Optimize command
@Command(name = "test", description = "Tests a dummy file using the analyzer. Specify the type of dummy file")
class TestDummyFile implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(TestDummyFile.class);

    @Parameters(paramLabel = "[type of file]", description = "type of file to analyze. Currently supports java")
    String typeOfFile;

    @Override
    public void run() {
        log.info("Running test dummy file option");
        log.info("The test file will be named dummy.* in the same directory as main");

        File dummyFile = new File("dummyFile", "dummyPath", "https://github.com/dummy/dummyRepo/raw/not_a_real_raw_url",
                "haaaaaash", "haaaaash again");

        dummyFile.save(); // Creates a project as well
        Path filePath;
        switch (typeOfFile.toLowerCase()) {
            case "java":
                log.debug("Analyzing dummy.java");
                filePath = Path.of("src/main/java/usf/edu/bronie/sqlcrawler/dummy.java");
                dummyFile.setLanguageType(Languages.JAVA);
                break;
            case "php":
                log.debug("Analyzing dummy.php");
                filePath = Path.of("src/main/java/usf/edu/bronie/sqlcrawler/dummy.php");
                dummyFile.setLanguageType(Languages.PHP);
                break;
            default:
                log.error("Unrecognizable file type ({})", typeOfFile.toLowerCase());
                return;
        }

        try {
            String code = Files.readString(filePath, StandardCharsets.US_ASCII);
            dummyFile.setCode(code);
        } catch (IOException e) {
            log.error("Error reading file", e);
            System.exit(-1);
        }

        // Now for the actual analysis!
        CodeAnalysisManager cam = new CodeAnalysisManager();

        Analysis a = cam.processFile(dummyFile);
        a.save();
        log.info("Successfully analyzed the dummy file");
        return;
    }
}