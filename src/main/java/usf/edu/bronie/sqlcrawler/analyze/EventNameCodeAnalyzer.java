package usf.edu.bronie.sqlcrawler.analyze;

import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.utils.RegexUtils;

import java.util.List;
import java.util.regex.Pattern;

public class EventNameCodeAnalyzer implements CodeAnalyzer {

    private String mStringLitWithPattern = RegexConstants.STRING_LITERAL_CONCAT_WITH_EVENT + RegexConstants.CONCAT_VAR;
    private String mStringLitPatternInterpolation = RegexConstants.STRING_LITERAL_CONCAT_WITH_EVENT + RegexConstants.INTERPOLATION_VAR;

    RegexConstants.Languages CompiledLang = null;
    Pattern stringLiteralPatternCompiled;
    Pattern mStringLitPatternInterpolationCompiled;

    
    private static final String DBFIELD = "event_usage";

    public String getDBField() {
        return DBFIELD;
    }

    @Override
    public SQLType analyzeCode(String code, List<String> sqlCodes, RegexConstants.Languages language) {
        if (!RegexUtils.hasSpecificSingleKeyword(sqlCodes, RegexConstants.EVENT_KEYWORD))
            return SQLType.NONE;

        String variable = RegexConstants.getVariable(language);
        String concat = RegexConstants.getConcat(language);
        String interpolationVariable = RegexConstants.getStringInterpolationTerm(language);

        if(language != CompiledLang){
            CompiledLang = language;
            stringLiteralPatternCompiled = Pattern.compile(String.format(mStringLitWithPattern,
                    concat,
                    variable),
                    Pattern.CASE_INSENSITIVE);
            
            mStringLitPatternInterpolationCompiled = Pattern.compile(
            		String.format(mStringLitPatternInterpolation, interpolationVariable), 
            		Pattern.CASE_INSENSITIVE);
        }
        
        if( RegexUtils.isSingleConcat(code, stringLiteralPatternCompiled) ) return SQLType.STRING_CONCAT;
        if(RegexUtils.isSingleConcat(code, mStringLitPatternInterpolationCompiled)) return SQLType.STRING_INTERP;
        else return SQLType.HARDCODED;
    }

    public SQLType analyzeCode(String code, RegexConstants.Languages language) {
        return null;
    }

}
