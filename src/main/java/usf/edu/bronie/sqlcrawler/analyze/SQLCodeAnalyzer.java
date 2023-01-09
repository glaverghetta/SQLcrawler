package usf.edu.bronie.sqlcrawler.analyze;

import org.apache.commons.lang3.StringUtils;
import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.utils.RegexUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLCodeAnalyzer implements CodeAnalyzer {
	//

    private String mStringLitWithVarPattern = RegexConstants.STRING_LITERAL_CONCAT_WITH_VAR_LOWER;

    private Pattern mAppendPattern = Pattern.compile(RegexConstants.APPEND_LOWER);

    private Pattern mStringFormatPattern = Pattern.compile(RegexConstants.STRING_FORMAT);

    private Pattern mJPAPreparedStatementPattern = Pattern.compile(RegexConstants.PREPARED_STATEMENT_KEYWORD_JPA);

    private static final String DBFIELD = "sql_usage_lower";
    
    RegexConstants.Languages CompiledLang = null;
    Pattern stringLiteralWithVarPatternCompiled;

    public String getDBField()
    {
        return DBFIELD;
    }

    @Override
    public SQLType analyzeCode(String code, List stringLiterals, RegexConstants.Languages language) {
        boolean isStringConcat = false;
        boolean isHardcoded = false;
        boolean containsStringFormat = code.contains(RegexConstants.STRING_FORMAT_KEYWORD);
        
        String variable = RegexConstants.getVariable(language);
        String concat = RegexConstants.getConcat(language);

        if(language != CompiledLang){
            CompiledLang = language;
            stringLiteralWithVarPatternCompiled = Pattern.compile(String.format(mStringLitWithVarPattern,
                    concat,
                    variable),
                    Pattern.CASE_INSENSITIVE);
        }

        Matcher stringLitWithVarMatcher = stringLiteralWithVarPatternCompiled.matcher(code);
        while(stringLitWithVarMatcher.find()) {
            String keyword = stringLitWithVarMatcher.group();
            if (RegexUtils.isSQLCode(keyword)) {
                isStringConcat = true;
                break;
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

        if (isStringConcat) {
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
