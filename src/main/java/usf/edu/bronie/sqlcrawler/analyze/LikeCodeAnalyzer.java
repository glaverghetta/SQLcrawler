package usf.edu.bronie.sqlcrawler.analyze;

import org.apache.commons.lang3.StringUtils;
import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.model.SQLType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LikeCodeAnalyzer implements CodeAnalyzer {

    private Pattern mStringLitPattern = Pattern.compile(RegexConstants.STRING_LITERAL);

    private Pattern mStringLitWithLikePattern = Pattern.compile(RegexConstants.STRING_LITERAL_CONCAT_WITH_LIKE,
            Pattern.CASE_INSENSITIVE);

    private Pattern mStringLikePrepPattern = Pattern.compile(RegexConstants.STRING_LITERAL_PREP_STATE_LIKE,
            Pattern.CASE_INSENSITIVE);

    public SQLType analyzeCode(String code) {
        if (!hasSpecificKeyword(code)) return SQLType.NONE;

        if (isLikeConcat(code)) {
            return SQLType.STRING_CONCAT;
        }

        if (isLikePrepStatement(code)) {
            return SQLType.PARAMATIZED_QUERY;
        }

        return SQLType.HARDCODED;
    }

    private boolean hasSpecificKeyword(String keyword) {
        if (StringUtils.containsIgnoreCase(keyword, RegexConstants.ORDER_BY_KEYWORD)) {
            return true;
        }

        return false;
    }

    private boolean isLikeConcat(String code) {
        Matcher stringLitWithOrderByMatcher = mStringLitWithLikePattern.matcher(code);
        while (stringLitWithOrderByMatcher.find()) {
            String keyword = stringLitWithOrderByMatcher.group();
            if (isSQLCode(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean isLikePrepStatement(String code) {
        Matcher stringLitMatcher = mStringLitPattern.matcher(code);
        while (stringLitMatcher.find()) {
            String keyword = stringLitMatcher.group();
            if (isSQLCode(keyword) && hasSpecificKeyword(keyword)) {
                Matcher matcher = mStringLikePrepPattern.matcher(keyword);
                if (matcher.find()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSQLCode(String group) {
        if (group == null) return false;

        for (String s : RegexConstants.SQL_KEYWORDS) {
            if (StringUtils.containsIgnoreCase(group, s)) {
                return true;
            }
        }
        return false;
    }
}
