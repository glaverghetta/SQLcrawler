package usf.edu.bronie.sqlcrawler.analyze;

import org.apache.commons.lang3.StringUtils;
import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.utils.RegexUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLCodeAnalyzer implements CodeAnalyzer {

    private Pattern mStringLitWithVarPattern = Pattern.compile(RegexConstants.STRING_LITERAL_CONCAT_WITH_VAR);

    private Pattern mStringLitPattern = Pattern.compile(RegexConstants.STRING_LITERAL);

    private Pattern mAppendPattern = Pattern.compile(RegexConstants.APPEND);

    private Pattern mStringFormatPattern = Pattern.compile(RegexConstants.STRING_FORMAT);

    private Pattern mJPAPreparedStatementPattern = Pattern.compile(RegexConstants.PREPARED_STATEMENT_KEYWORD_JPA);

    public SQLType analyzeCode(String code) {
        boolean isStringConcat = false;
        boolean isPreparedStatement = false;
        boolean isHardcoded = false;
        boolean containsStringFormat = code.contains(RegexConstants.STRING_FORMAT_KEYWORD);

        Matcher stringLitWithVarMatcher = mStringLitWithVarPattern.matcher(code);
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

        Matcher stringLitMatcher = mStringLitPattern.matcher(code);
        while(stringLitMatcher.find()) {
            String keyword = stringLitMatcher.group();
            if (RegexUtils.isSQLCode(keyword)) {
                if (hasPreparedStatement(keyword)) {
                    isPreparedStatement= true;
                    isHardcoded = false;
                    break;
                } else if (containsStringFormat && mStringFormatPattern.matcher(keyword).find()) {
                    isStringConcat = true;
                } else {
                    isHardcoded = true;
                }
            }
        }

        if (isStringConcat && isPreparedStatement) {
            return SQLType.PARAMATIZED_QUERY_AND_CONCAT;
        } else if (isStringConcat) {
            return SQLType.STRING_CONCAT;
        } else if (isPreparedStatement) {
            return SQLType.PARAMATIZED_QUERY;
        } else if (isHardcoded) {
            return SQLType.HARDCODED;
        }

        return SQLType.NONE;
    }

    private boolean hasPreparedStatement(String group) {
        if (!StringUtils.containsIgnoreCase(group, RegexConstants.PREPARED_STATEMENT_KEYWORD)) {
            Matcher stringLitMatcher = mJPAPreparedStatementPattern.matcher(group);
            return stringLitMatcher.find();
        }
        return true;
    }
}
