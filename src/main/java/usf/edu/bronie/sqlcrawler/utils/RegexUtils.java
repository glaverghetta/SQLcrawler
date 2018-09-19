package usf.edu.bronie.sqlcrawler.utils;

import org.apache.commons.lang3.StringUtils;
import usf.edu.bronie.sqlcrawler.constants.RegexConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {

    private static Pattern mStringLitWithVarPattern = Pattern.compile(RegexConstants.STRING_LITERAL);

    public static boolean hasSpecificKeyword(String code, String keyword) {
        Matcher stringLitWithVarMatcher = mStringLitWithVarPattern.matcher(code);
        while (stringLitWithVarMatcher.find()) {
            String group = stringLitWithVarMatcher.group();
            if (isSQLCode(group) && StringUtils.containsIgnoreCase(group, keyword)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSQLCode(String group) {
        if (group == null) return false;

        for (String s : RegexConstants.SQL_KEYWORDS) {
            if (StringUtils.containsIgnoreCase(group, s)) {
                return true;
            }
        }
        return false;
    }
}
