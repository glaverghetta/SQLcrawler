package usf.edu.bronie.sqlcrawler.analyze;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;

import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.utils.RegexUtils;
import usf.edu.bronie.sqlcrawler.constants.RegexConstants.Languages;

public class LikeCodeAnalyzerTest {

    @ParameterizedTest
    @CsvSource(value={
        "   main{'SELECT abc FROM table WHERE abc LIKE \'123\''}   ~     true",
        " $data = \"SELECT abc FROM table WHERE abc LIKE \'%$abc%\'\";   ~     false",
        "   main{'SELECT abc FROM table WHERE abc LIKE \'%$abc%\''}   ~     false",
        "   main{'SELECT abc FROM table WHERE $abc LIKE \'abc\''}   ~     true",
    }, delimiter='~')
    void testPHPHardCode(String code, boolean containsHardCoded) {
        LikeCodeAnalyzer testA = new LikeCodeAnalyzer();

        List<String> stringLiterals = RegexUtils.findAllStringLiteral(code);

        SQLType result = testA.analyzeCode(code, stringLiterals, Languages.nameToLang("PHP"));

        assertEquals(containsHardCoded, result == SQLType.HARDCODED);
    }

    @ParameterizedTest
    @CsvSource(value={
        " $data = \"SELECT abc FROM table WHERE abc LIKE $abc\";   %     true",
        " $data = \"SELECT abc FROM table WHERE abc LIKE '%$abc%'\";   %     true",
        " $data = \"SELECT abc FROM table WHERE abc LIKE '$abc'\";   %     true",
        " $data = \"SELECT abc FROM table WHERE abc LIKE '{$abc}'\";   %     true",
        " $data = \"SELECT abc FROM table WHERE abc LIKE '%{$abc}%'\";   %     true",
    }, delimiter='%')
    void testPHPInterp(String code, boolean constainsInterp) {
        LikeCodeAnalyzer testA = new LikeCodeAnalyzer();

        List<String> stringLiterals = RegexUtils.findAllStringLiteral(code);

        SQLType result = testA.analyzeCode(code, stringLiterals, Languages.nameToLang("PHP"));

        assertEquals(constainsInterp, result == SQLType.STRING_INTERP);
    }
}
