package usf.edu.bronie.sqlcrawler.analyze;

import org.apache.commons.lang3.StringUtils;
import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.utils.RegexUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLCodeAnalyzer implements CodeAnalyzer {

    private Pattern mStringLitWithVarPattern = Pattern.compile(RegexConstants.STRING_LITERAL_CONCAT_WITH_VAR_LOWER);

    private Pattern mAppendPattern = Pattern.compile(RegexConstants.APPEND_LOWER);

    private Pattern mStringFormatPattern = Pattern.compile(RegexConstants.STRING_FORMAT);

    private Pattern mJPAPreparedStatementPattern = Pattern.compile(RegexConstants.PREPARED_STATEMENT_KEYWORD_JPA);

    private static final String DBFIELD = "sql_usage_lower";

    public String getDBField()
    {
        return DBFIELD;
    }

    @Override
    public SQLType analyzeCode(String code, List stringLiterals) {
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

        List<String> l = stringLiterals;
        for (String keyword: l) {
            if (RegexUtils.isSQLCode(keyword)) {
                if (hasPreparedStatement(keyword)) {
                    isPreparedStatement = true;
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

    public SQLType analyzeCode(String code) {
        return null;
    }

    private boolean hasPreparedStatement(String group) {
        if (!StringUtils.containsIgnoreCase(group, RegexConstants.PREPARED_STATEMENT_KEYWORD)) {
            Matcher stringLitMatcher = mJPAPreparedStatementPattern.matcher(group);
            return stringLitMatcher.find();
        }
        return true;
    }
}
