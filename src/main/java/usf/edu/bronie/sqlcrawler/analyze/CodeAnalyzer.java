package usf.edu.bronie.sqlcrawler.analyze;

import org.apache.commons.lang3.StringUtils;
import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.model.SQLType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeAnalyzer {

    private Pattern mStringLitWithVarPattern = Pattern.compile(RegexConstants.STRING_LITERAL_CONCAT_WITH_VAR);

    private Pattern mStringLitPattern = Pattern.compile(RegexConstants.STRING_LITERAL);

    private Pattern mAppendPattern = Pattern.compile(RegexConstants.APPEND);

    public SQLType analyzeCode(String code) {
        boolean isStringConcat = false;
        boolean isPreparedStatement = false;
        boolean isHardcoded = false;

        Matcher stringLitWithVarMatcher = mStringLitWithVarPattern.matcher(code);
        while(stringLitWithVarMatcher.find()) {
            String keyword = stringLitWithVarMatcher.group();
            if (isSQLCode(keyword)) {
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
            if (isSQLCode(keyword)) {
                if (hasPreparedStatement(keyword)) {
                    isPreparedStatement= true;
                    isHardcoded = false;
                    break;
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
        return StringUtils.containsIgnoreCase(group, RegexConstants.PREPARED_STATEMENT_KEYWORD);
    }

    private boolean isSQLCode(String group) {
        if (group == null) return false;

        for (String s: RegexConstants.SQL_KEYWORDS) {
            if (StringUtils.containsIgnoreCase(group, s)) {
                return true;
            }
        }
        return false;
    }
}
