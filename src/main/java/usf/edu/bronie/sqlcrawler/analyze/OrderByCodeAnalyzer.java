package usf.edu.bronie.sqlcrawler.analyze;

import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.utils.RegexUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrderByCodeAnalyzer implements CodeAnalyzer {

    private Pattern mStringLitWithOrderByPattern = Pattern.compile(RegexConstants.STRING_LITERAL_CONCAT_WITH_ORDER_BY,
            Pattern.CASE_INSENSITIVE);

    public SQLType analyzeCode(String code) {
        if (!RegexUtils.hasSpecificKeyword(code, RegexConstants.ORDER_BY_KEYWORD)) return SQLType.NONE;

        boolean orderByConcat = isOrderByConcat(code);

        return orderByConcat ? SQLType.STRING_CONCAT : SQLType.HARDCODED;
    }

    private boolean isOrderByConcat(String code) {
        Matcher stringLitWithOrderByMatcher = mStringLitWithOrderByPattern.matcher(code);
        while (stringLitWithOrderByMatcher.find()) {
            String keyword = stringLitWithOrderByMatcher.group();
            if (RegexUtils.isSQLCode(keyword)) {
                return true;
            }
        }
        return false;
    }
}
