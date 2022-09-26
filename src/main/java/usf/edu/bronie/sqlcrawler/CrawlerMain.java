package usf.edu.bronie.sqlcrawler;

import usf.edu.bronie.sqlcrawler.analyze.CodeAnalyzer;
import usf.edu.bronie.sqlcrawler.analyze.GroupOrderByCodeAnalyzer;
import usf.edu.bronie.sqlcrawler.crawler.GithubCrawler;
import usf.edu.bronie.sqlcrawler.io.GithubAPI.RateLimitException;
import usf.edu.bronie.sqlcrawler.io.GithubAPI.SecondaryLimitException;
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
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrawlerMain {

    private static final Logger log = LoggerFactory.getLogger(CrawlerMain.class);

    public static void main(String[] args) {
        // Create a fake file
        // Same as above, this won't create a new file on subsequent runs
        // unless you update the file name or path, whic might be useful (i.e., name the
        // file "TriggerTest")
        // File dummyFile = new File("dummyFile", "dummyPath",
        // "https://github.com/dummy/dummyRepo/raw/not_a_real_raw_url",
        // "haaaaaash", "haaaaash again");

        // dummyFile.save(); //Creates a project as well

        // // Now load the code directly from a file instead of pulling from a url.
        // // Note that the getCode function will visit the url if not already set,
        // // so by setting it now we bypass that.
        // // I placed my file in the same folder as main
        // Path filePath =
        // Path.of("src/main/java/usf/edu/bronie/sqlcrawler/dummy.java");

        // try {
        // String code = Files.readString(filePath, StandardCharsets.US_ASCII);
        // dummyFile.setCode(code);
        // } catch (IOException e) {
        // System.out.println("Error reading file");
        // System.out.println(e);
        // System.exit(-1);
        // }

        // //Now for the actual analysis!
        // CodeAnalysisManager cam = new CodeAnalysisManager();
        // Analysis a = cam.processFile(dummyFile);
        // a.save();
        // System.out.println("Successfully analyzed the dummy file");
        // return;

        // Here is a SQL query to retrieve the results from the DB
        // SELECT f.filename, a.* FROM crawler.analyses a LEFT JOIN crawler.projects p
        // ON p.id = a.project LEFT JOIN crawler.files f ON f.id = a.file WHERE
        // p.name="dummy/dummyRepo"

        // Kevin's code testing the Github provider

        CodeAnalysisManager cam = new CodeAnalysisManager();
        GithubProvider scp = new GithubProvider();

        

        int updated = 0;
        int total = 0;
        int WAIT = 60;

        for(int i = 10; i < 15; i++){
            while(true){
                try{
                    scp.addAllUrlsByPage(i, "Java");
                    break;
                }catch(SecondaryLimitException e){
                    log.debug("Hit secondary limit, waiting {} seconds", WAIT);
                    try        
                    {
                        TimeUnit.SECONDS.sleep(WAIT);
                    } 
                    catch(InterruptedException ex) 
                    {
                        Thread.currentThread().interrupt();
                    }
                }catch(RateLimitException e){
                    log.debug("{}", e);

                    long time = e.getResetTime() + 1 - Instant.now().getEpochSecond();
                    try        
                    {
                        if(time > 0){
                            TimeUnit.SECONDS.sleep(time); 
                        }
                    } 
                    catch(InterruptedException ex) 
                    {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            while (scp.hasNext()) {
                total++;
                File result = scp.receiveNextData();
                if (result.save())
                    updated++;
                Analysis a = cam.processFile(result);
                a.save();
            }
            log.debug("Finished scanning page {}", i);
        }

        log.info("Scanned and analyzed {} files, added {} new files", total, updated);

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

    }
}