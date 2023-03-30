package usf.edu.bronie.sqlcrawler.analyze;

import java.util.List;
import java.util.regex.Pattern;

import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.utils.RegexUtils;

public class GroupOrderByCodeAnalyzer implements CodeAnalyzer {

    private String mStringLitPattern = RegexConstants.GROUP_ORDER_BY + RegexConstants.CONCAT_VAR;
    private String mStringLitPatternMultiple = RegexConstants.GROUP_ORDER_BY + RegexConstants.CONCAT_VAR_MULTIPLE;
    private String mStringLitPatternInterpolation = RegexConstants.GROUP_ORDER_BY + RegexConstants.INTERPOLATION_VAR;


    RegexConstants.Languages lastUsedLang = null;
    Pattern stringLiteralPatternJava;
    Pattern stringLitPatternMultipleJava;
    Pattern mStringLitPatternInterpolationCompiled;


    private static final String DBFIELD = "order_group_usage";

    public String getDBField() {
        return DBFIELD;
    }

    @Override
    public SQLType analyzeCode(String code, List<String> sqlCodes, RegexConstants.Languages language) {
        if (!RegexUtils.hasSpecificKeyword(sqlCodes, RegexConstants.GROUP_ORDER_BY_KEYWORD))
            return SQLType.NONE;

        String variable = RegexConstants.getVariable(language);
        String concat = RegexConstants.getConcat(language);
        String interpolationVariable = RegexConstants.getStringInterpolationTerm(language);

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
            mStringLitPatternInterpolationCompiled = Pattern.compile(
            		String.format(mStringLitPatternInterpolation, interpolationVariable), 
            		Pattern.CASE_INSENSITIVE);
        }

        if(RegexUtils.isConcat(code, stringLiteralPatternJava)) return SQLType.STRING_CONCAT;
        if(RegexUtils.isConcat(code, stringLitPatternMultipleJava)) return SQLType.STRING_CONCAT_LIST;
        if(RegexUtils.isConcat(code, mStringLitPatternInterpolationCompiled)) return SQLType.STRING_INTERP;
        else return SQLType.HARDCODED;
   
    }

    public SQLType analyzeCode(String code, RegexConstants.Languages language) {
        return null;
    }

}
