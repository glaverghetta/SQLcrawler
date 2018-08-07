package usf.edu.bronie.sqlcrawler.analyze;

import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.utils.RegexUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroupByCodeAnalyzer implements CodeAnalyzer {

    private Pattern mStringLitWithGroupByPattern = Pattern.compile(RegexConstants.STRING_LITERAL_CONCAT_WITH_GROUP_BY,
            Pattern.CASE_INSENSITIVE);

    public SQLType analyzeCode(String code) {
        if (!RegexUtils.hasSpecificKeyword(code, RegexConstants.GROUP_BY_KEYWORD)) return SQLType.NONE;

        boolean groupByConcat = isGroupByConcat(code);

        return groupByConcat ? SQLType.STRING_CONCAT : SQLType.HARDCODED;
    }

    private boolean isGroupByConcat(String code) {
        Matcher stringLitWithGroupByMatcher = mStringLitWithGroupByPattern.matcher(code);
        while (stringLitWithGroupByMatcher.find()) {
            String keyword = stringLitWithGroupByMatcher.group();
            if (RegexUtils.isSQLCode(keyword)) {
                return true;
            }
        }
        return false;
    }
}
