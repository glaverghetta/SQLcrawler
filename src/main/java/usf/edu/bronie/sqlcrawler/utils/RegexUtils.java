package usf.edu.bronie.sqlcrawler.utils;

import org.apache.commons.lang3.StringUtils;
import usf.edu.bronie.sqlcrawler.constants.RegexConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {

    private static Pattern mStringLitWithVarPattern = Pattern.compile(RegexConstants.STRING_LITERAL);

    public static boolean hasSpecificKeyword(String code, String[] keywords) {
        Matcher stringLitWithVarMatcher = mStringLitWithVarPattern.matcher(code);
        while (stringLitWithVarMatcher.find()) {
            String group = stringLitWithVarMatcher.group();
            if (isSQLCode(group)) {
                for (int i = 0; i < keywords.length; i++) {
                    if (StringUtils.containsIgnoreCase(group, keywords[i])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasSpecificKeyword(List<String> codes, String[] keywords) {
        for (String group : codes) {
            if (isSQLCode(group)) {
                for (int i = 0; i < keywords.length; i++) {
                    if (StringUtils.containsIgnoreCase(group, keywords[i])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasSpecificSingleKeyword(List<String> codes, String[] keywords) {
        for (String group : codes) {
            for (int i = 0; i < keywords.length; i++) {
                if (StringUtils.containsIgnoreCase(group, keywords[i])) {
                    return true;
                }
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

    public static List findAllStringLiteral(String code) {
        List l = new ArrayList();
        Matcher stringLitWithVarMatcher = mStringLitWithVarPattern.matcher(code);
        while (stringLitWithVarMatcher.find()) {
            String group = stringLitWithVarMatcher.group();
            l.add(group);
        }
        return l;
    }

    public static boolean isConcat(String code, Pattern pattern) {
        Matcher matcher = pattern.matcher(code);
        while (matcher.find()) {
            String keyword = matcher.group();
            if (RegexUtils.isSQLCode(keyword)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSingleConcat(String code, Pattern pattern) {
        Matcher matcher = pattern.matcher(code);
        while (matcher.find()) {
            return true;
        }
        return false;
    }
}
