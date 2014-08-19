package org.n3r.diamond.sdk.utils;

/**
 * 模糊查询时合成 sql的工具类
 */
public class PatternUtils {

    /**
     * 检查参数字符串中是否包含符号 '*'
     *
     * @param patternStr
     * @return 包含返回true, 否则返回false
     */
    public static boolean hasCharPattern(String patternStr) {
        if (patternStr == null)
            return false;
        String pattern = patternStr;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '*')
                return true;
        }
        return false;
    }


    /**
     * 替换掉所有的符号'*'为'%'
     *
     * @param sourcePattern
     * @return 返回替换后的字符串
     */
    public static String generatePattern(String sourcePattern) {
        if (sourcePattern == null)
            return "";
        StringBuilder sb = new StringBuilder();
        String pattern = sourcePattern;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '*')
                sb.append('%');
            else
                sb.append(c);
        }
        return sb.toString();
    }

}
