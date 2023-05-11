package usf.edu.bronie.sqlcrawler.analyze;

import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.utils.RegexUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LikeCodeAnalyzer implements CodeAnalyzer {

    private Pattern mStringLitPattern = Pattern.compile(RegexConstants.STRING_LITERAL);

    private String mStringLitWithLikePattern = RegexConstants.STRING_LITERAL_CONCAT_WITH_LIKE + RegexConstants.CONCAT_VAR;
    private String mStringLitPatternInterpolation = RegexConstants.STRING_LITERAL_CONCAT_WITH_LIKE + RegexConstants.INTERPOLATION_VAR;


    // This one does not take any string formatters currently
    private Pattern mStringLikePrepPattern = Pattern.compile(RegexConstants.STRING_LITERAL_PREP_STATE_LIKE,
            Pattern.CASE_INSENSITIVE);
    
    RegexConstants.Languages CompiledLang = null;
    Pattern stringLiteralLikePatternCompiled;
    Pattern mStringLitPatternInterpolationCompiled;
            
    private static final String DBFIELD = "like_usage";

    public String getDBField() {
        return DBFIELD;
    }

    @Override
    public SQLType analyzeCode(String code, List<String> sqlCodes, RegexConstants.Languages language) {
        // return null; //Old code
        return analyzeCode(code, language);
    }

    public SQLType analyzeCode(String code, RegexConstants.Languages language) {
        if (!RegexUtils.hasSpecificKeyword(code, RegexConstants.LIKE_KEYWORD))
            return SQLType.NONE;

        if (isLikeConcat(code, language)) {
            return SQLType.STRING_CONCAT;
        }

        if (isLikeInterp(code, language)) {
            return SQLType.STRING_INTERP;
        }

        if (isLikePrepStatement(code)) {
            return SQLType.PARAMATIZED_QUERY;
        }

        return SQLType.HARDCODED;
    }

    private boolean containsLike(String keyword) {
        // if (StringUtils.containsIgnoreCase(keyword, RegexConstants.LIKE_KEYWORD)) {
        // return true;
        // }

        return false;
    }

    private boolean isLikeConcat(String code, RegexConstants.Languages language) {
    	String variable = RegexConstants.getVariable(language);
        String concat = RegexConstants.getConcat(language);
        
    	if(language != CompiledLang){
            CompiledLang = language;
            stringLiteralLikePatternCompiled = Pattern.compile(String.format(mStringLitWithLikePattern,
                    concat,
                    variable),
                    Pattern.CASE_INSENSITIVE);
  
        }
    	
        Matcher stringLitWithOrderByMatcher = stringLiteralLikePatternCompiled.matcher(code);
        while (stringLitWithOrderByMatcher.find()) {
            String keyword = stringLitWithOrderByMatcher.group();
            if (RegexUtils.isSQLCode(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean isLikeInterp(String code, RegexConstants.Languages language) {
        String interpolationVariable = RegexConstants.getStringInterpolationTerm(language);
        
    	if(language != CompiledLang || mStringLitPatternInterpolationCompiled == null){
            CompiledLang = language;
            mStringLitPatternInterpolationCompiled = Pattern.compile(
            		String.format(mStringLitPatternInterpolation, interpolationVariable), 
            		Pattern.CASE_INSENSITIVE);
  
        }
        Matcher stringLitWithOrderByMatcherInterpolation = mStringLitPatternInterpolationCompiled.matcher(code);
        while (stringLitWithOrderByMatcherInterpolation.find()) {
            String keyword = stringLitWithOrderByMatcherInterpolation.group();
            if (RegexUtils.isSQLCode(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean isLikePrepStatement(String code) {
        Matcher stringLitMatcher = mStringLitPattern.matcher(code);
        while (stringLitMatcher.find()) {
            String keyword = stringLitMatcher.group();
            if (RegexUtils.isSQLCode(keyword) && containsLike(keyword)) {
                Matcher matcher = mStringLikePrepPattern.matcher(keyword);
                if (matcher.find()) {
                    return true;
                }
            }
        }
        return false;
    }
}
