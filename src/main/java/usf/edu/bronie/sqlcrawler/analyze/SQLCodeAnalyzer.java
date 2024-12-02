package usf.edu.bronie.sqlcrawler.analyze;

import org.apache.commons.lang3.StringUtils;
import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.utils.RegexUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLCodeAnalyzer implements CodeAnalyzer {
	//

    private String mStringLitWithVarPattern = RegexConstants.STRING_LITERAL_CONCAT_WITH_VAR_LOWER;
    
    private String mStringLitWithVarPatternStringInterpolation = RegexConstants.STRING_LITERAL_STRING_INTERPOLATION_CONCAT;

    private Pattern mAppendPattern = Pattern.compile(RegexConstants.APPEND_LOWER);

    private Pattern mStringFormatPattern = Pattern.compile(RegexConstants.STRING_FORMAT);

    private Pattern mJPAPreparedStatementPattern = Pattern.compile(RegexConstants.PREPARED_STATEMENT_KEYWORD_JPA);

    private static final String DBFIELD = "sql_usage_lower";
    
    RegexConstants.Languages CompiledLang = null;
    Pattern stringLiteralWithVarPatternCompiled;
    Pattern stringLiteralWithVarPatternInterpolationCompiled;

    public String getDBField()
    {
        return DBFIELD;
    }

    @Override
    public SQLType analyzeCode(String code, List<String> stringLiterals, RegexConstants.Languages language) {
        boolean isStringConcat = false;
        boolean isHardcoded = false;
        boolean isStringInterp = false;
        boolean containsStringFormat = code.contains(RegexConstants.STRING_FORMAT_KEYWORD);
        
        String variable = RegexConstants.getVariable(language);
        String concat = RegexConstants.getConcat(language);
        String interpolationVariable = RegexConstants.getStringInterpolationTerm(language);

        if(language != CompiledLang){
            CompiledLang = language;
            stringLiteralWithVarPatternCompiled = Pattern.compile(String.format(mStringLitWithVarPattern,
                    concat,
                    variable),
                    Pattern.CASE_INSENSITIVE);
            stringLiteralWithVarPatternInterpolationCompiled = Pattern.compile(String.format(
            		mStringLitWithVarPatternStringInterpolation,
            		interpolationVariable), Pattern.CASE_INSENSITIVE);
            
        }
        
        
        Matcher stringLitWithVarMatcher = stringLiteralWithVarPatternCompiled.matcher(code);
        while(stringLitWithVarMatcher.find()) {
            String keyword = stringLitWithVarMatcher.group();
            if (RegexUtils.isSQLCode(keyword)) {
                isStringConcat = true;
                break;
            }
        }
        
        // Interpolation concatenation checking
        if(interpolationVariable != "") {
            Matcher stringLitWithVarInterpolationMatcher = stringLiteralWithVarPatternInterpolationCompiled.matcher(code);
            while(stringLitWithVarInterpolationMatcher.find()) {
                String keyword = stringLitWithVarInterpolationMatcher.group();
                if (RegexUtils.isSQLCode(keyword)) {
                    isStringInterp = true;
                    break;
                }
            }
        }	

        if (!isStringConcat) {
            Matcher appendMatcher = mAppendPattern.matcher(code);
            if (appendMatcher.find()) {
                isStringConcat = true;
            }
        }

        List<String> l = stringLiterals;

        for (String keyword: l) {
            if (RegexUtils.isSQLCode(keyword)) {
                if (containsStringFormat && mStringFormatPattern.matcher(keyword).find()) {
                    isStringConcat = true;
                } else {
                    isHardcoded = true;
                }
            }
        }

        if (isStringInterp) {
            return SQLType.STRING_INTERP;
        }else if (isStringConcat) {
            return SQLType.STRING_CONCAT;
        } else if (isHardcoded) {
            return SQLType.HARDCODED;
        } 

        return SQLType.NONE;
    }

    public SQLType analyzeCode(String code, RegexConstants.Languages language) {
        return null;
    }

    public boolean hasPreparedStatement(String group, RegexConstants.Languages language) {
    	String preparedStatement = RegexConstants.getPreparedStatementTerm(language);

        if (!StringUtils.containsIgnoreCase(group, preparedStatement)) {
        	// This code is for other ways to find parameterized queries 
            Matcher stringLitMatcher = mJPAPreparedStatementPattern.matcher(group);
            return stringLitMatcher.find();
        }
        return true;
    }
}
