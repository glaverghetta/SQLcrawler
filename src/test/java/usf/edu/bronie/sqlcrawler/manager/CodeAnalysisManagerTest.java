package usf.edu.bronie.sqlcrawler.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import usf.edu.bronie.sqlcrawler.constants.CredentialConstants;
import usf.edu.bronie.sqlcrawler.constants.RegexConstants.Languages;
import usf.edu.bronie.sqlcrawler.model.Analysis;
import usf.edu.bronie.sqlcrawler.model.ApiType;
import usf.edu.bronie.sqlcrawler.model.File;
import usf.edu.bronie.sqlcrawler.model.SQLType;

public class CodeAnalysisManagerTest {

    private final static String TESTCSV = "/javaTests.csv";
    private static String csvHeaderKey;
    private final static int csvHeaderKeyIndex = 4;

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

        dummyFile.save();  //Saves to test database
    
        return dummyFile;
    }

    @BeforeAll
    public static void initialize() throws IOException {

        CredentialConstants.loadConfigResource("/testConfig.json");

        InputStream in = CodeAnalysisManagerTest.class.getResourceAsStream(CodeAnalysisManagerTest.TESTCSV);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String headerLine = br.readLine();
        System.out.println(headerLine);
        CodeAnalysisManagerTest.csvHeaderKey = headerLine.split(",")[CodeAnalysisManagerTest.csvHeaderKeyIndex];
        in.close();
    }

    @ParameterizedTest
    @CsvFileSource(resources=CodeAnalysisManagerTest.TESTCSV)
    void testProcessFileJava(String fileName, String sql_usage, Boolean is_parameterized, String api_type, String key) throws IOException {
        CodeAnalysisManager cam = new CodeAnalysisManager();

        HashMap<String, SQLType> expectedResults = splitAnswers(key, CodeAnalysisManagerTest.csvHeaderKey);

        String code = new String(CodeAnalysisManagerTest.class.getResourceAsStream("/java/" + fileName).readAllBytes());

        Analysis results = cam.processFile(createDummyFile(code, Languages.JAVA));

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
