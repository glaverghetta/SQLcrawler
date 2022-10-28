package usf.edu.bronie.sqlcrawler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
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

    private static final Logger finalLog = LogManager.getLogger("FinalLogger");

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new CrawlerMain());
        if (args.length == 0) {
            cmd.usage(System.out);
        } else {
            // Use a lambda to avoid somwhat expensive string join operation if logging
            // disabled
            finalLog.info("Arguments provided: {}", () -> String.join(" ~ ", args));
            cmd.execute(args);
        }
    }
}



@Command(name = "cs", description = "Testing C# github API")
class CS implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(CS.class);

    @Override
    public void run() {
        // Kevin's code testing the Github provider

        CodeAnalysisManager cam = new CodeAnalysisManager();
        GithubAPI gh = new GithubAPI(1, 1000,  Languages.CSHARP);

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

    private static final Logger log = LogManager.getLogger(Pull.class);

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

    private static final Logger log = LogManager.getLogger(Analyze.class);

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

    private static final Logger log = LogManager.getLogger(Statistics.class);

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

    private static final Logger log = LogManager.getLogger(Optimize.class);
    private static final Logger finalLog = LogManager.getLogger("FinalLogger");
    private static final Logger frameLog = LogManager.getLogger("FrameLogger");
    private static final Logger pageLog = LogManager.getLogger("PageLogger");
    private static final Logger fileLog = LogManager.getLogger("FileLogger");

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

        long startTime = System.currentTimeMillis();
        int startSize = minSize;

        if (minSize > stopPoint) {
            log.error("Starting point is greater than max value");
            return;
        }

        log.info("Running optimized mode for {} up to {} bytes, starting window {}-{}{}", typeOfFile, stopPoint,
                minSize, minSize + startingWindow, !noShrink ? "" : " (no window shrinking)");
        int maxSize = minSize + startingWindow;

        Languages lang = Languages.nameToLang(typeOfFile);

        CodeAnalysisManager cam = new CodeAnalysisManager();
        GithubAPI gh = new GithubAPI(minSize, maxSize, lang);

        int updated = 0;
        int total = 0;
        int lastTotal = 0;
        int totalNumPages = 0;

        while (minSize < stopPoint) {
            int framePages = 0;
            int frameShrink = 0;
            int frameGrowth = 0;
            long frameStart = System.currentTimeMillis();
            while (gh.isNextPage()) {
                Queue<File> results = null;

                long pageStart = System.currentTimeMillis();
                try {
                    results = gh.searchSleep();
                } catch (PageLimitException e) {
                    log.error("", e);
                    System.exit(-1);
                }

                totalNumPages++;
                framePages++;

                int pageSize = results.size();
                while (!results.isEmpty()) {
                    total++;
                    File result = results.poll();
                    // Only analyze if this is a new file (may be some repeats when shrinking window
                    // size)
                    if (result.save()) {
                        updated++;
                        long fileStart = System.currentTimeMillis();
                        Analysis a = cam.processFile(result);
                        long fileEnd = System.currentTimeMillis();
                        fileLog.info("{} ~ {} ~ {} ~ {} ~ {} ~ {} ~ {} ~ {}", new Date(fileStart), new Date(fileEnd),
                                fileEnd - fileStart, result.getId(), result.getCodeSize(), gh.lastPagePulled(), minSize, maxSize);
                        a.save();
                    }
                }
                long pageEnd = System.currentTimeMillis();
                pageLog.info("{} ~ {} ~ {} ~ {} ~ {} ~ {} ~ {}", new Date(pageStart), new Date(pageEnd),
                        pageEnd - pageStart, gh.lastPagePulled(), minSize, maxSize, pageSize);

                if (minSize != maxSize) {
                    log.debug(
                            "Finished scanning page {} for files between {} and {} bytes (page had {} results, frame has {}, {} in total)",
                            gh.lastPagePulled(), minSize, maxSize,
                            total - lastTotal, gh.getLastTotalCount(), total);
                } else {
                    log.debug("Finished scanning page {} for files of size {} bytes (page had {} results, frame has {}, {} total)",
                            gh.lastPagePulled(), minSize,
                            total - lastTotal, gh.getLastTotalCount(), total);
                }

                lastTotal = total;

                // Check if we need to shrink the window
                if (gh.getLastTotalCount() > 1000 && minSize != maxSize && !noShrink) {
                    // Shrink the window by half
                    frameShrink++;
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

                //Check if the frame is too small - only grow if we haven't already shrunk on this frame
                if (gh.getLastTotalCount() < 500 && !noShrink && frameShrink == 0) {
                    // Shrink the window by half
                    frameGrowth++;
                    int diff = (maxSize - minSize) / 2;
                    if (diff == 0) {
                        maxSize = maxSize + 1; // Just a single byte size now!
                        log.debug("Got {} results. Grew the search window to {}-{} bytes.", gh.getLastTotalCount(),
                                minSize, maxSize);
                    } else {
                        maxSize = maxSize + diff;
                        log.debug("Got {} results. Grew the search window to {}-{} bytes.", gh.getLastTotalCount(),
                                minSize, maxSize);
                    }
                    //Growth is for next frame to avoid flipping back and forth. Don't call gh.setSize here
                }
            }

            if (minSize != maxSize) {
                log.info("Scanned and analyzed {} files so far, added {} new files between {} and {} bytes ", total,
                        updated, minSize, maxSize);
            } else {
                log.info("Scanned and analyzed {} files so far, added {} new files of size {} bytes ", total, updated,
                        minSize);
            }

            long frameEnd = System.currentTimeMillis();
            frameLog.info("{} ~ {} ~ {} ~ {} ~ {} ~ {} ~ {} ~ {} ~ {} ~ {}", new Date(frameStart), new Date(frameEnd),
                    frameEnd - frameStart, minSize, maxSize,
                    updated, framePages, gh.getLastTotalCount(), frameShrink, frameGrowth);

            // Increase the range
            int diff = maxSize - minSize;
            minSize += diff + 1;
            maxSize += diff + 1;
            gh.setSize(minSize, maxSize);
        }

        long endTime = System.currentTimeMillis();
        finalLog.info("Total files: {} ~ Total pages: {} ~ Start: {} ~ End: {} ~ Time in MS: {}", total,
                totalNumPages, new Date(startTime), new Date(endTime), endTime - startTime);
    }
}

// Optimize command
@Command(name = "test", description = "Tests a dummy file using the analyzer. Specify the type of dummy file")
class TestDummyFile implements Runnable {

    private static final Logger log = LogManager.getLogger(TestDummyFile.class);

    @Parameters(paramLabel = "[type of file]", description = "type of file to analyze. Currently supports java, php, cs")
    String typeOfFile;

    @Override
    public void run() {
        log.info("Running test dummy file option");
        log.info("The test file will be named dummy.* in the same directory as main");

        File dummyFile = new File("dummy." + typeOfFile, "dummyPath",
                "https://github.com/dummy/dummyRepo/raw/not_a_real_raw_url",
                "haaaaaash", "haaaaash again", Languages.nameToLang(typeOfFile));

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
            case "cs":
            	log.debug("Analyzing dummy.cs");
            	filePath = Path.of("src/main/java/usf/edu/bronie/sqlcrawler/dummy.cs");
            	dummyFile.setLanguageType(Languages.CSHARP);
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