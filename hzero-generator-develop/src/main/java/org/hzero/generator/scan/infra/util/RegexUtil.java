package org.hzero.generator.scan.infra.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lijb
 */
public class RegexUtil {

    /**
     * 获取首次匹配到的字符串
     * @param str
     * @param groupIndex
     * @param pattern
     * @return
     */
    public static String getFirstMatcher(String str,int groupIndex,Pattern pattern){
        Matcher m=pattern.matcher(str);
        while (m.find()){
            String mstr=m.group(groupIndex);
            if(mstr!=null){
                return mstr;
            }
        }
        return null;
    }

    /**
     * 获取首次匹配到的字符串的位置
     * @param str
     * @param groupIndex
     * @param pattern
     * @return
     */
    public static int getFirstMatcherIndex(String str,int groupIndex,Pattern pattern){
        Matcher m=pattern.matcher(str);
        while (m.find()){
            String mstr=m.group(groupIndex);
            if(mstr!=null){
                return m.end();
            }
        }
        return -1;
    }

    /**
     * 获取匹配到的所有字符串
     * @param str
     * @param groupIndex
     * @param pattern
     * @return
     */
    public static List<String> getAllMatcher(String str,int groupIndex,Pattern pattern){
        List<String> l=new ArrayList<>();
        Matcher m=pattern.matcher(str);
        while (m.find()){
            String mstr=m.group(groupIndex);
            if(mstr!=null){
                l.add(mstr);
            }
        }
        return l;
    }
}
