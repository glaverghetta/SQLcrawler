package usf.edu.bronie.sqlcrawler.analyze;

import java.util.List;
import java.util.regex.Pattern;

import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.utils.RegexUtils;

public class TableNameCodeAnalyzer implements CodeAnalyzer {

    private String mStringLitPattern = RegexConstants.TABLE + RegexConstants.CONCAT_VAR;
    private String mStringLitPatternMultiple = RegexConstants.TABLE + RegexConstants.CONCAT_VAR_MULTIPLE;

    RegexConstants.Languages lastUsedLang = null;
    Pattern stringLiteralPatternJava;
    Pattern stringLitPatternMultipleJava;
    
    private static final String DBFIELD = "table_usage";

    public String getDBField() {
        return DBFIELD;
    }

    @Override
    public SQLType analyzeCode(String code, List sqlCodes, RegexConstants.Languages language) {
        if (!RegexUtils.hasSpecificKeyword(sqlCodes, RegexConstants.TABLE_KEYWORD))
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
        
        if(RegexUtils.isConcat(code, stringLiteralPatternJava)) return SQLType.STRING_CONCAT;
        if(RegexUtils.isConcat(code, stringLitPatternMultipleJava)) return SQLType.STRING_CONCAT_LIST;
        else return SQLType.HARDCODED;

    
    }

    public SQLType analyzeCode(String code, RegexConstants.Languages language) {
        return null;
    }
}
