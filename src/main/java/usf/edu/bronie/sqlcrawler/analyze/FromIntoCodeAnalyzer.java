package usf.edu.bronie.sqlcrawler.analyze;

import usf.edu.bronie.sqlcrawler.constants.RegexConstants;
import usf.edu.bronie.sqlcrawler.model.SQLType;
import usf.edu.bronie.sqlcrawler.utils.RegexUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FromIntoCodeAnalyzer implements CodeAnalyzer {

    private Pattern mStringLitWithFromIntoPattern = Pattern.compile(RegexConstants.STRING_LITERAL_CONCAT_WITH_FROM_INTO,
            Pattern.CASE_INSENSITIVE);

    public SQLType analyzeCode(String code) {
        if (!RegexUtils.hasSpecificKeyword(code, RegexConstants.FROM_KEYWORD) &&
                !RegexUtils.hasSpecificKeyword(code, RegexConstants.INTO_KEYWORD) &&
                !RegexUtils.hasSpecificKeyword(code, RegexConstants.UPDATE_KEYWORD))
            return SQLType.NONE;

        boolean fromIntoByConcat = isFromIntoByConcat(code);

        return fromIntoByConcat ? SQLType.STRING_CONCAT : SQLType.HARDCODED;
    }

    private boolean isFromIntoByConcat(String code) {
        Matcher stringLitWithFromIntoByMatcher = mStringLitWithFromIntoPattern.matcher(code);
        while (stringLitWithFromIntoByMatcher.find()) {
            String keyword = stringLitWithFromIntoByMatcher.group();
            if (RegexUtils.isSQLCode(keyword)) {
                return true;
            }
        }
        return false;
    }
}
