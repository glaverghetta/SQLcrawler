package usf.edu.bronie.sqlcrawler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import usf.edu.bronie.sqlcrawler.constants.CredentialConstants;
import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.constants.RegexConstants.Languages;
import usf.edu.bronie.sqlcrawler.io.DBConnection;
import usf.edu.bronie.sqlcrawler.io.GithubAPI;
import usf.edu.bronie.sqlcrawler.io.GithubAPI.PageLimitException;
import usf.edu.bronie.sqlcrawler.manager.CodeAnalysisManager;
import usf.edu.bronie.sqlcrawler.model.Analysis;
import usf.edu.bronie.sqlcrawler.model.File;
import usf.edu.bronie.sqlcrawler.model.Project;
import usf.edu.bronie.sqlcrawler.model.Project.noProjectFound;

@Command(name = "SQLCrawler", subcommands = { CommandLine.HelpCommand.class, Pull.class, Repo.class, Analyze.class,
        Statistics.class, Optimize.class,
        TestDummyFile.class }, description = "Tool for analyzing SQLIDIA vulnerabilities")
public class CrawlerMain {

    private static final Logger finalLog = LogManager.getLogger("FinalLogger");

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new CrawlerMain());
        if (args.length == 0) {
            cmd.usage(System.out);
            System.exit(-1);
        } else {
            // Use a lambda to avoid somewhat expensive string join operation if logging
            // disabled
            finalLog.info("Arguments provided: {}", () -> String.join(" ~ ", args));
            int exitCode = cmd.execute(args);
            System.exit(exitCode);
        }
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
    private static final Logger fileLog = LogManager.getLogger("FileLogger");

    @Option(names = { "--config-file" }, description = "Config file to load", arity = "1", defaultValue = "./config.json")
    String configFile;

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
        log.info("Running the classifier on all files without an entry in the analysis database");
        CredentialConstants.loadConfigFile(configFile);

        try {
            CodeAnalysisManager cam = new CodeAnalysisManager();
            Connection mConnection = DBConnection.getConnection();
            PreparedStatement statement;
            // Switch XXX and YYY to ranges. We will remove those entirely when we are done fixing
            statement = mConnection.prepareStatement("SELECT id from files WHERE id NOT IN (SELECT file FROM analyses) AND id >= XXX AND id < YYY");

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                File result = new File(resultSet.getInt("id"));

                long fileStart = System.currentTimeMillis();
                Analysis a = cam.processFile(result);
                long fileEnd = System.currentTimeMillis();
                fileLog.info("{} ~ {} ~ {} ~ {} ~ {} ~ {} ~ {} ~ {}", new Date(fileStart),
                        new Date(fileEnd), fileEnd - fileStart, result.getId(), result.getCodeSize(), 0, 0, 0);
                a.save();
            }
            statement.close();
            mConnection.close();
        } catch (SQLException e) {
            // Todo: For now, just print error and quit. Might want to add more complicated
            // solution in the future
            log.error("Error retrieving files from database", e);
            System.exit(-1);
        }
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

@Command(name = "repo", description = "Pulls repository info for projects without stats")
class Repo implements Runnable {

    private static final Logger log = LogManager.getLogger(Repo.class);

    @Option(names = { "--batch-size" }, description = "Query N projects at once", arity = "1", defaultValue = "100")
    int batchSize;

    @Option(names = { "--config-file" }, description = "Config file to load", arity = "1", defaultValue = "./config.json")
    String configFile;

    @Option(names = { "--all-projects" }, description = "Pull and update stats for all projects in the database")
    boolean allProjects;

    @Override
    public void run() {
        log.info("Pulling repository data");

        CredentialConstants.loadConfigFile(configFile);

        Set<Project> projectsToScan = new HashSet<Project>();

        // These values don't matter
        GithubAPI gh = new GithubAPI(1, 100, Languages.nameToLang("Java"));

        try {
            Connection mConnection = DBConnection.getConnection();
            PreparedStatement statement;
            if (!allProjects) {
                statement = mConnection
                        .prepareStatement("SELECT id from projects WHERE id NOT IN (SELECT project from repo_info)");
            } else {
                statement = mConnection.prepareStatement("SELECT id FROM projects");
            }

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                projectsToScan.add(new Project(resultSet.getInt("id")));

                if (projectsToScan.size() > batchSize) {
                    gh.projectSleep(projectsToScan, batchSize);
                }
            }
            statement.close();
            mConnection.close();
        } catch (SQLException e) {
            // Todo: For now, just print error and quit. Might want to add more complicated
            // solution in the future
            log.error("Error retrieving projects from database", e);
            System.exit(-1);
        }

        // Finish any remaining queued projects
        while (projectsToScan.size() > 0) {
            gh.projectSleep(projectsToScan, batchSize);
        }
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

    @Parameters(paramLabel = "[language]", description = "the language to search for SQLIDIAs. Currently supports Java, PHP, JS, C#")
    String typeOfFile;

    @Parameters(paramLabel = "[max size in bytes]", description = "pull files until search window passes X bytes in size (that is, may pull results beyond X if window is large)")
    int stopPoint;

    @Option(names = { "--no-shrink" }, description = "Don't decrease the search window size if > 1000 results returned")
    boolean noShrink;

    @Option(names = { "--onePage" }, description = "Only retrieve one page of results from each frame")
    boolean stopAfterOnePage;

    @Option(names = {
            "--start" }, description = "The size to start searching with. Must be less than the specified max search size", arity = "1", defaultValue = "1")
    int minSize;

    @Option(names = {
        "--end" }, description = "The max size of the frame to begin searching with. Must be less than the specified max search size. If set, --window is ignored", arity = "1", defaultValue = "0")
    int windowEnd;

    @Option(names = {
            "--start-page" }, description = "Start scanning the first frame on page N (used for resuming interrupted scanning)", arity = "1", defaultValue = "1")
    int startPage;

    @Option(names = {
            "--window" }, description = "The beginning size of the search window", arity = "1", defaultValue = "1000")
    int startingWindow;

    @Option(names = {
            "--project-scan" }, description = "Pull repo stats after queueing N projects", arity = "1", defaultValue = "100")
    int projectScan;

    @Option(names = {
            "--early" }, description = "Continue to next frame after obtaining a certain amount of records", arity = "1", defaultValue = "9999")
    int earlyContinue; // Default 9999; max is only 1000, so this translates to reading all possible
                       // values
    
    @Option(names = { "--config-file" }, description = "Config file to load", arity = "1", defaultValue = "./config.json")
    String configFile;

    private Set<Project> projectsToScan = new HashSet<Project>();

    public void addProjectToScan(Project p) {
        if (p.hasStats()) {
            return;
        }

        this.projectsToScan.add(p);
    }

    @Override
    public void run() {
        CredentialConstants.loadConfigFile(configFile);
        long startTime = System.currentTimeMillis();

        // int startSize = minSize;

        if (minSize > stopPoint) {
            log.error("Starting point is greater than max value");
            return;
        }

        int maxSize = minSize + startingWindow;

        if(windowEnd != 0){
            maxSize = windowEnd;
        }

        log.info("Running optimized mode for {} up to {} bytes, starting window {}-{}{}", typeOfFile, stopPoint,
                minSize, maxSize, !noShrink ? "" : " (no window shrinking)");
        
        Languages lang = Languages.nameToLang(typeOfFile);

        CodeAnalysisManager cam = new CodeAnalysisManager();
        GithubAPI gh = new GithubAPI(minSize, maxSize, lang);

        gh.setPage(startPage);

        int updated = 0;
        int total = 0;
        int lastTotal = 0;
        int totalNumPages = 0;

        while (minSize < stopPoint) {
            int framePages = 0;
            int frameShrink = 0;
            long frameStart = System.currentTimeMillis();
            int recordsInFrame = 0;
            Boolean repeated = false;

            // Check if there is another page and we have not reached minimum records to
            // continue
            while (gh.isNextPage() && recordsInFrame < earlyContinue) {
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
                        recordsInFrame++;
                        long fileStart = System.currentTimeMillis();
                        Analysis a = cam.processFile(result);
                        long fileEnd = System.currentTimeMillis();
                        fileLog.info("{} ~ {} ~ {} ~ {} ~ {} ~ {} ~ {} ~ {}", new Date(fileStart),
                                new Date(fileEnd),
                                fileEnd - fileStart, result.getId(), result.getCodeSize(),
                                gh.lastPagePulled(), minSize,
                                maxSize);
                        a.save();
                    }
                    try {
                        addProjectToScan(new Project(result.getProject()));
                    } catch (noProjectFound e) {
                        // TODO Auto-generated catch block
                        log.error("Unable to find project id for file id {}", result.getId(), e);
                        System.exit(-1);
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
                    log.debug(
                            "Finished scanning page {} for files of size {} bytes (page had {} results, frame has {}, {} total)",
                            gh.lastPagePulled(), minSize,
                            total - lastTotal, gh.getLastTotalCount(), total);
                }

                if (total == lastTotal && !repeated) {
                    log.debug("Last page had zero results, repeating page once");
                    repeated = true;
                    gh.setPage(gh.lastPagePulled());
                } else {
                    if(total == lastTotal){
                        log.debug("Last page had zero results twice, continuing");
                    }
                    repeated = false;
                }

                lastTotal = total;

                // Check if we need to shrink the frame
                if (gh.getLastTotalCount() > 1000 && minSize != maxSize && !noShrink) {
                    // Shrink the window by half
                    frameShrink++;
                    int diff = (maxSize - minSize) / 2;
                    if (diff == 0) {
                        maxSize = minSize; // Just a single byte size now!
                        log.debug("Got {} results. Shrunk the search window to {} bytes.",
                                gh.getLastTotalCount(),
                                minSize);
                    } else {
                        maxSize = minSize + diff;
                        log.debug("Got {} results. Shrunk the search window to {}-{} bytes.",
                                gh.getLastTotalCount(),
                                minSize, maxSize);
                    }
                    gh.setSize(minSize, maxSize);  //Need to switch to new frame now to avoid data loss
                }

                log.debug("Currently queued {} projects to scan", projectsToScan.size());
                if (projectsToScan.size() > projectScan) {
                    gh.projectSleep(projectsToScan, projectScan);
                }

                if(stopAfterOnePage && !repeated){
                    break;
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
            frameLog.info("{} ~ {} ~ {} ~ {} ~ {} ~ {} ~ {} ~ {} ~ {}", new Date(frameStart), new Date(frameEnd),
                    frameEnd - frameStart, minSize, maxSize,
                    updated, framePages, gh.getLastTotalCount(), frameShrink);

            log.debug("Currently queued {} projects to scan", projectsToScan.size());
            if (projectsToScan.size() > projectScan) {
                gh.projectSleep(projectsToScan, projectScan);
            }

            // Move to next frame
            int diff = maxSize - minSize;
            minSize += diff + 1;
            maxSize += diff + 1;

            // Check if the new frame should grow in size
            if (gh.getLastTotalCount() < 500 && !noShrink) {
                // Increase the frame size by half
                diff = (maxSize - minSize) / 2;
                if (diff == 0) {
                    maxSize = maxSize + 1;
                    log.debug("Github reported {} results in last frame. Grew the search window to {}-{} bytes.",
                            gh.getLastTotalCount(),
                            minSize, maxSize);
                } else {
                    maxSize = maxSize + diff;
                    log.debug("Github reported {} results in last frame. Grew the search window to {}-{} bytes.",
                            gh.getLastTotalCount(),
                            minSize, maxSize);
                }
            }

            gh.setSize(minSize, maxSize);
        }

        // Finish all the projects left to scan
        while (projectsToScan.size() > 0) {
            gh.projectSleep(projectsToScan, projectScan);
        }

        long endTime = System.currentTimeMillis();
        finalLog.info("Total files: {} ~ Total pages: {} ~ Start: {} ~ End: {} ~ Time in MS: {}", total,
                totalNumPages, new Date(startTime), new Date(endTime), endTime - startTime);
    }
}

// Optimize command
//TODO: Get rid of this in place of unit tests
@Command(name = "test", description = "Tests a dummy file using the analyzer. Specify the type of dummy file")
class TestDummyFile implements Runnable {

    private static final Logger log = LogManager.getLogger(TestDummyFile.class);

    @Parameters(paramLabel = "[type of file]", description = "type of file to analyze. Currently supports java, php, cs")
    String typeOfFile;

    @Override
    public void run() {
        log.info("Running test dummy file option");
        log.info("The test file will be named dummy.* in the same directory as main");

        File dummyFile = new File("dummyProjectID", "dummy." + typeOfFile, "dummyPath",
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
            case "js":
                log.debug("Analyzing dummy.js");
                filePath = Path.of("src/main/java/usf/edu/bronie/sqlcrawler/dummy.js");
                dummyFile.setLanguageType(Languages.JS);
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
        a.printResults();
        log.info("Successfully analyzed the dummy file");
        return;
    }
}