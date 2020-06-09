package org.hzero.generator.util;

/**
 * 生成器字符串工具类
 * @author xianzhi.chen@hand-china.com	2018年6月28日上午10:53:56
 */
public class GenStringUtils {
    
    public static final char UNDERLINE = '_';
    
    public static final char HORIZONTALLINE = '-';
	
    /**
     * 过滤DDL中SQL关键字
     * @param str
     * @return
     */
	public static String SqlFilter(String str) {
		String[] pattern = { "select", "insert", "delete", "from", "count\\(", "drop table", "update", "truncate",
				"asc\\(", "mid\\(", "char\\(", "xp_cmdshell", "exec   master", "netlocalgroup administrators",
				"net user", "CR", "LF", "BS", "or", "and",";" };
		for (int i = 0; i < pattern.length; i++) {
			str = str.replace(pattern[i].toString(), "");
		}
		return str;
	}
	
	/**
	 * 驼峰转中横线
	 * @param param
	 * @return
	 */
	public static String camelToHorizontalline(String param) {
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append(HORIZONTALLINE);
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
