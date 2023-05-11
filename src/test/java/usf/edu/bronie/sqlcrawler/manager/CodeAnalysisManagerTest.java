package usf.edu.bronie.sqlcrawler.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import usf.edu.bronie.sqlcrawler.constants.CredentialConstants;
import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.constants.RegexConstants.Languages;
import usf.edu.bronie.sqlcrawler.model.Analysis;
import usf.edu.bronie.sqlcrawler.model.ApiType;
import usf.edu.bronie.sqlcrawler.model.File;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.model.File.rawGitHubLinkInvalid;

public class CodeAnalysisManagerTest {

    private final static String TESTJAVACSV = "/javaTests.csv";
    private final static String TESTPHPCSV = "/phpTests.csv";
    private static String csvHeaderKey;
    private final static int csvHeaderKeyIndex = 4;
    private static final Logger log = LogManager.getLogger(CodeAnalysisManagerTest.class);

    private static HashMap<String, SQLType> splitAnswers(String key, String headerKey){
        HashMap<String, SQLType> ret = new HashMap<>();

        String[] keyList =  key.split("~");
        String[] headerList =  headerKey.split("~");
        for(int i = 0; i < keyList.length; i++){
            ret.put(headerList[i], SQLType.valueOf(keyList[i]));
        }
        
        return ret;
    }

    public static File createDummyFile(String code, Languages lang){
        File dummyFile = new File("dummyProjectID", "dummy.txt", "dummyPath",
                "https://github.com/dummy/dummyRepo/raw/not_a_real_raw_url",
                "haaaaaash", "haaaaash again", lang);
        dummyFile.setCode(code);

        try{
            dummyFile.save();  //Saves to test database
        }
        catch(rawGitHubLinkInvalid e){
            log.info(e);
        }
    
        return dummyFile;
    }

    @BeforeAll
    public static void initialize() throws IOException {

        CredentialConstants.loadConfigResource("/testConfig.json");

        InputStream in = CodeAnalysisManagerTest.class.getResourceAsStream(CodeAnalysisManagerTest.TESTJAVACSV);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String headerLine = br.readLine();
        System.out.println(headerLine);
        CodeAnalysisManagerTest.csvHeaderKey = headerLine.split(",")[CodeAnalysisManagerTest.csvHeaderKeyIndex];
        in.close();
    }

    @ParameterizedTest
    @CsvFileSource(resources=CodeAnalysisManagerTest.TESTPHPCSV)
    void testProcessFilePHP(String fileName, String sql_usage, Boolean is_parameterized, String api_type, String key) throws IOException {
        CodeAnalysisManager cam = new CodeAnalysisManager();

        HashMap<String, SQLType> expectedResults = splitAnswers(key, CodeAnalysisManagerTest.csvHeaderKey);

        String code = new String(CodeAnalysisManagerTest.class.getResourceAsStream("/php/" + fileName).readAllBytes());

        Analysis results = null;
        try{
            results = cam.processFile(createDummyFile(code, Languages.PHP));
        }
        catch(rawGitHubLinkInvalid e){
            log.info(e);
            System.exit(-1);
        }

        SQLType got = results.getSql_usage();
        SQLType expected = SQLType.valueOf(sql_usage);

        assertTrue(expected == got);
        assertTrue(is_parameterized == results.isParameterized());
        assertTrue(ApiType.valueOf(api_type) == results.getApi_type());

        for (var entry : results.getResults().entrySet()) {
            got = entry.getValue();
            expected = expectedResults.get(entry.getKey());
            assertTrue(got == expected);
        }
    }

    @ParameterizedTest
    @CsvFileSource(resources=CodeAnalysisManagerTest.TESTJAVACSV)
    void testProcessFileJava(String fileName, String sql_usage, Boolean is_parameterized, String api_type, String key) throws IOException {
        CodeAnalysisManager cam = new CodeAnalysisManager();

        HashMap<String, SQLType> expectedResults = splitAnswers(key, CodeAnalysisManagerTest.csvHeaderKey);

        String code = new String(CodeAnalysisManagerTest.class.getResourceAsStream("/java/" + fileName).readAllBytes());

        Analysis results = null;
        try{
            results = cam.processFile(createDummyFile(code, Languages.JAVA));
        }
        catch(rawGitHubLinkInvalid e){
            log.info(e);
            System.exit(-1);
        }

        assertTrue(SQLType.valueOf(sql_usage) == results.getSql_usage());
        assertTrue(is_parameterized == results.isParameterized());
        assertTrue(ApiType.valueOf(api_type) == results.getApi_type());

        for (var entry : results.getResults().entrySet()) {
            SQLType got = entry.getValue();
            SQLType expected = expectedResults.get(entry.getKey());
            assertTrue(got == expected);
        }
    }
}
