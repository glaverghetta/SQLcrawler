package usf.edu.bronie.sqlcrawler.analyze;

import java.util.List;
import java.util.regex.Pattern;

import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.utils.RegexUtils;

public class ColumnNameCodeAnalyzer implements CodeAnalyzer {
    private String mStringLitPattern = RegexConstants.STRING_LITERAL_COLUMN + RegexConstants.CONCAT_VAR;
    private String mStringLitPatternMultiple = RegexConstants.STRING_LITERAL_COLUMN
            + RegexConstants.CONCAT_VAR_MULTIPLE;
    
    RegexConstants.Languages lastUsedLang = null;
    Pattern stringLiteralPatternJava;
    Pattern stringLitPatternMultipleJava;

    // private Pattern mStringLitPattern =
    // Pattern.compile(RegexConstants.STRING_LITERAL_COLUMN +
    // RegexConstants.CONCAT_VAR,
    // Pattern.CASE_INSENSITIVE);

    // private Pattern mStringLitPatternMultiple =
    // Pattern.compile(RegexConstants.STRING_LITERAL_COLUMN +
    // RegexConstants.CONCAT_VAR,
    // Pattern.CASE_INSENSITIVE);

    private static final String DBFIELD = "column_usage";

    public String getDBField() {
        return DBFIELD;
    }

    @Override
    public SQLType analyzeCode(String code, List sqlCodes, RegexConstants.Languages language) {
        if (!RegexUtils.hasSpecificKeyword(sqlCodes, RegexConstants.COLUMN_KEYWORD))
            return SQLType.NONE;

        String variable = RegexConstants.getVariable(language);
        String concat = RegexConstants.getConcat(language);

        if(language != lastUsedLang){
            lastUsedLang = language;
            stringLiteralPatternJava = Pattern.compile(String.format(mStringLitPattern,
                    concat,
                    variable),
                    Pattern.CASE_INSENSITIVE);
    
            stringLitPatternMultipleJava = Pattern.compile(String.format(mStringLitPatternMultiple,
                    variable,
                    concat,
                    variable),
                    Pattern.CASE_INSENSITIVE);
        }


        return RegexUtils.isConcat(code, stringLiteralPatternJava)
                || RegexUtils.isConcat(code, stringLitPatternMultipleJava) ? SQLType.STRING_CONCAT : SQLType.HARDCODED;

    }

    public SQLType analyzeCode(String code) {
        return null;
    }
}
