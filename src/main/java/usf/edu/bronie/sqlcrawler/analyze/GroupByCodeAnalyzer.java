package usf.edu.bronie.sqlcrawler.analyze;

import org.apache.commons.lang3.StringUtils;
import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.model.SQLType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroupByCodeAnalyzer implements CodeAnalyzer {

    private Pattern mStringLitWithGroupByPattern = Pattern.compile(RegexConstants.STRING_LITERAL_CONCAT_WITH_GROUP_BY,
            Pattern.CASE_INSENSITIVE);

    public SQLType analyzeCode(String code) {
        if (!hasSpecificKeyword(code)) return SQLType.NONE;

        boolean groupByConcat = isGroupByConcat(code);

        return groupByConcat ? SQLType.STRING_CONCAT : SQLType.HARDCODED;
    }

    private boolean hasSpecificKeyword(String keyword) {
        if (StringUtils.containsIgnoreCase(keyword, RegexConstants.GROUP_BY_KEYWORD)) {
            return true;
        }

        return false;
    }

    private boolean isGroupByConcat(String code) {
        Matcher stringLitWithGroupByMatcher = mStringLitWithGroupByPattern.matcher(code);
        while (stringLitWithGroupByMatcher.find()) {
            String keyword = stringLitWithGroupByMatcher.group();
            if (isSQLCode(keyword)) {
                return true;
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
