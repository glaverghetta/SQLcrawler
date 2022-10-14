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
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters; //K: Is ISOCodeResolve the app name (I assume from an example)? Should this be SQLCrawler?
import usf.edu.bronie.sqlcrawler.constants.RegexConstants.Languages;
import usf.edu.bronie.sqlcrawler.io.GithubAPI;
import usf.edu.bronie.sqlcrawler.io.GithubAPI.PageLimitException;
import usf.edu.bronie.sqlcrawler.manager.CodeAnalysisManager;
import usf.edu.bronie.sqlcrawler.model.Analysis;
import usf.edu.bronie.sqlcrawler.model.File;

@Command(name = "SQLCrawler", subcommands = { CommandLine.HelpCommand.class, Pull.class, Analyze.class,
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

    @Parameters(paramLabel = "[language]", description = "the language to search for SQLIDIAs. Currently supports Java, PHP")
    String typeOfFile;

    @Parameters(paramLabel = "[max size in bytes]", description = "pull files until search window passes X bytes in size (that is, may pull results beyond X if window is large)")
    int stopPoint;

    @Option(names = { "--no-shrink" }, description = "Don't decrease the search window size if > 1000 results returned")
    boolean noShrink;

    @Option(names = {
            "--start" }, description = "The size to start searching with. Must be less than the specified max search size", arity = "1", defaultValue = "1")
    int minSize;

    @Option(names = {
            "--window" }, description = "The beginning size of the search window", arity = "1", defaultValue = "1000")
    int startingWindow;

    @Override
    public void run() {
        if(startingWindow > stopPoint){
            log.error("Starting point is greater than max value");
            return;
        }
        log.info("Running optimized mode for {} up to {} bytes, starting window {}-{}{}", typeOfFile, stopPoint,
                minSize, minSize + startingWindow, !noShrink ? "" : " (no window shrinking)");
        int maxSize = minSize + startingWindow;
        Languages lang = null;

        switch (typeOfFile.toLowerCase()) {
            case "java":
                lang = Languages.JAVA;
                break;
            case "php":
                lang = Languages.PHP;
                break;
            case "c#":
                lang = Languages.CSHARP;
                break;
            default:
                log.error("Unrecognizable file type ({})", typeOfFile.toLowerCase());
                return;
        }

        CodeAnalysisManager cam = new CodeAnalysisManager();
        GithubAPI gh = new GithubAPI(minSize, maxSize, lang);

        int updated = 0;
        int total = 0;
        int lastTotal = 0;

        while (minSize < stopPoint) {
            while (gh.isNextPage()) {
                Queue<File> results = null;

                try {
                    results = gh.searchSleep();
                } catch (PageLimitException e) {
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

                if (minSize != maxSize) {
                    log.debug(
                            "Finished scanning page {} for files between {} and {} bytes (page had {} results, {} total)",
                            gh.lastPagePulled(), minSize, maxSize,
                            total - lastTotal, total);
                } else {
                    log.debug("Finished scanning page {} for files of size {} bytes (page had {} results, {} total)",
                            gh.lastPagePulled(), minSize,
                            total - lastTotal, total);
                }

                lastTotal = total;

                // Check if we need to shrink the window
                if (gh.getLastTotalCount() > 1000 && minSize != maxSize && !noShrink) {
                    // Shrink the window by half
                    int diff = (maxSize - minSize) / 2;
                    if (diff == 0) {
                        maxSize = minSize; // Just a single byte size now!
                        log.debug("Got {} results. Shrunk the search window to {} bytes.", gh.getLastTotalCount(),
                                minSize);
                    } else {
                        maxSize = minSize + diff;
                        log.debug("Got {} results. Shrunk the search window to {}-{} bytes.", gh.getLastTotalCount(),
                                minSize, maxSize);
                    }
                    gh.setSize(minSize, maxSize);
                }
            }

            if (minSize != maxSize) {
                log.info("Scanned and analyzed {} files so far, added {} new files between {} and {} bytes ", total,
                        updated, minSize, maxSize);
            } else {
                log.info("Scanned and analyzed {} files so far, added {} new files of size {} bytes ", total, updated,
                        minSize);
            }

            // Increase the range
            int diff = maxSize - minSize;
            minSize += diff + 1;
            maxSize += diff + 1;
            gh.setSize(minSize, maxSize);
        }
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

        File dummyFile = new File("dummy." + typeOfFile, "dummyPath",
                "https://github.com/dummy/dummyRepo/raw/not_a_real_raw_url",
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