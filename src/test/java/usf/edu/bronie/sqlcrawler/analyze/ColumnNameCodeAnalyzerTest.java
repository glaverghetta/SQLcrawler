package usf.edu.bronie.sqlcrawler.analyze;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;

import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.utils.RegexUtils;
import usf.edu.bronie.sqlcrawler.constants.RegexConstants.Languages;

public class ColumnNameCodeAnalyzerTest {
    // @FileParameters("src/test/resources/test.csv")
    @ParameterizedTest
    @CsvSource(value={
        "   main{'SELECT ' + a + ' FROM table'}   %     false",
        "   main{'SELECT * FROM table;'}    %     true",
        "   main{'SELECT column FROM table;'}    %     true",
        "   main{'SELECT colA, colB FROM table;'}    %     true",
        "   main{'SELECT * FROM table, ' + a;}    %     true",
        "   main{'SELECT ' + a + ', ' + b + ' FROM table;'}    %     false",
        "   main{'SELECT a, ' + b + ' FROM table;'}    %     false",
        "   main{'SELECT ${abc} FROM table;'}    %     false",
    }, delimiter='%')
    void testJavaHardCode(String code, boolean containsHardCoded) {
        ColumnNameCodeAnalyzer testA = new ColumnNameCodeAnalyzer();

        List<String> stringLiterals = RegexUtils.findAllStringLiteral(code);

        SQLType result = testA.analyzeCode(code, stringLiterals, Languages.nameToLang("Java"));

        assertEquals(containsHardCoded, result == SQLType.HARDCODED);
    }

    @ParameterizedTest
    @CsvSource(value={
        "   main{'SELECT ' + a + ' FROM table'}   %     true",
        "   main{'SELECT * FROM table;'}    %     false",
        "   main{'SELECT column FROM table;'}    %     false",
        "   main{'SELECT colA, colB FROM table;'}    %     false",
        "   main{'SELECT * FROM table, ' + a;}    %     false",
        "   main{'SELECT ' + a + ', ' + b + ' FROM table;'}    %     true",
        "   main{'SELECT a, ' + b + ' FROM table;'}    %     false",
        "   main{'SELECT ${abc} FROM table;'}    %     false",
    }, delimiter='%')
    void testJavaConcat(String code, boolean containsHardCoded) {
        ColumnNameCodeAnalyzer testA = new ColumnNameCodeAnalyzer();

        List<String> stringLiterals = RegexUtils.findAllStringLiteral(code);

        SQLType result = testA.analyzeCode(code, stringLiterals, Languages.nameToLang("Java"));

        assertEquals(containsHardCoded, result == SQLType.STRING_CONCAT);
    }

    @ParameterizedTest
    @CsvSource(value={
        "   main{'SELECT ' + a + ' FROM table'}   %     false",
        "   main{'SELECT * FROM table;'}    %     false",
        "   main{'SELECT column FROM table;'}    %     false",
        "   main{'SELECT colA, colB FROM table;'}    %     false",
        "   main{'SELECT * FROM table, ' + a;}    %     false",
        "   main{'SELECT ' + a + ', ' + b + ' FROM table;'}    %     false",
        "   main{'SELECT a, ' + b + ' FROM table;'}    %     true",
        "   main{'SELECT ${abc} FROM table;'}    %     false",
    }, delimiter='%')
    void testJavaConcatList(String code, boolean containsHardCoded) {
        ColumnNameCodeAnalyzer testA = new ColumnNameCodeAnalyzer();

        List<String> stringLiterals = RegexUtils.findAllStringLiteral(code);

        SQLType result = testA.analyzeCode(code, stringLiterals, Languages.nameToLang("Java"));

        assertEquals(containsHardCoded, result == SQLType.STRING_CONCAT_LIST);
    }

    @ParameterizedTest
    @CsvSource(value={
        "   main{'SELECT ' + a + ' FROM table'}   %     false",
        "   main{'SELECT * FROM table;'}    %     false",
        "   main{'SELECT column FROM table;'}    %     false",
        "   main{'SELECT colA, colB FROM table;'}    %     false",
        "   main{'SELECT * FROM table, ' + a;}    %     false",
        "   main{'SELECT ' + a + ', ' + b + ' FROM table;'}    %     false",
        "   main{'SELECT a, ' + b + ' FROM table;'}    %     false",
        "   main{'SELECT ${abc} FROM table;'}    %     true",
        "   main{'SELECT ${0} FROM table;'}    %     true",
        "   main{'SELECT {abc} FROM table;'}    %     true",
        "   main{'SELECT {0} FROM table;'}    %     true",
    }, delimiter='%')
    void testJavaInterp(String code, boolean containsHardCoded) {
        ColumnNameCodeAnalyzer testA = new ColumnNameCodeAnalyzer();

        List<String> stringLiterals = RegexUtils.findAllStringLiteral(code);

        SQLType result = testA.analyzeCode(code, stringLiterals, Languages.nameToLang("Java"));

        assertEquals(containsHardCoded, result == SQLType.STRING_INTERP);
    }

    @Test
    void testAnalyzeCode2() {

    }
}
