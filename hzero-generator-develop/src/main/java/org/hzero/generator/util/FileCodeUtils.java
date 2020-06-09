package org.hzero.generator.util;

import org.apache.commons.collections.CollectionUtils;
import org.hzero.generator.export.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 文件处理
 *
 * @author liguo.wang
 */
public class FileCodeUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileCodeUtils.class);

    /**
     * 获得该目录下的所有目录名
     *
     * @param path 目录
     * @return 该目录下的所有目录名
     */
    public static List<String> getDirectory(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        if (files != null) {
            List<String> list = new ArrayList<>();
            for (File f : files) {
                if (f.isDirectory()) {
                    list.add(f.getName());
                }
            }
            return list;
        }
        return new ArrayList<>();
    }

    public static void fileReplaceUpdate(String localPath) {
        File file = new File(localPath);
        File[] files = file.listFiles();
        if (files != null) {
            StringBuffer log = new StringBuffer();
            log.append("配置文件升级完成:");
            for (File serviceFile : files) {
                String serviceName = serviceFile.getName();
                String servicePath = XmlUtils.SERVICE_PATH.get(serviceName);
                File[] fileList = serviceFile.listFiles();
                if (serviceFile.isDirectory() && fileList != null) {
                    log.append("\n------------------------------\n");
                    log.append("服务：");
                    log.append(serviceName);
                    for (File configFile : fileList) {
                        if (configFile.isFile()) {
                            File targetFile = new File(servicePath + "/" + Constants.RESOURCES_PREFIX + configFile.getName());
                            if (targetFile.exists()) {
                                try {
                                    execute(configFile, targetFile, log);
                                } catch (IOException e) {
                                    logger.error("升级配置文件失败 >>>>> {}", e.getMessage());
                                }
                            }
                        }
                    }
                }
            }
            logger.info(log.toString());
        }
    }

    /**
     * 执行替换更新
     *  @param newestFile 当前最新文件
     * @param targetFile 要被升级的目标文件
     * @param log 日志记录
     */
    private static void execute(File newestFile, File targetFile, StringBuffer log) throws IOException {
        BufferedReader newestReader = new BufferedReader(new FileReader(newestFile));
        BufferedReader targetReader = new BufferedReader(new FileReader(targetFile));
        String s1;
        String s2;
        ArrayList<String> newestList = new ArrayList<>();
        ArrayList<String> targetList = new ArrayList<>();
        while ((s1 = newestReader.readLine()) != null) {
            newestList.add(s1);
        }
        while ((s2 = targetReader.readLine()) != null) {
            targetList.add(s2);
        }
        targetList.removeAll(newestList);
        log.append("\n升级文件：");
        log.append(targetFile.getName());
        log.append("\n");
        if (targetList.isEmpty()) {
            log.append("升级内容：无");
        } else {
            ArrayList<String> updateList = new ArrayList<>();
            for (String s : targetList) {
                if (trimLeft(s).length() == s.length()) {
                    break;
                }
                updateList.add(s);
            }
            targetList.removeAll(updateList);
            if (CollectionUtils.isNotEmpty(updateList)) {
                log.append("被更新配置：\n");
                updateList.forEach(s -> {
                    log.append(s);
                    log.append("\n");
                });
            }
            if (CollectionUtils.isNotEmpty(targetList)) {
                log.append("继承原配置：\n");
                targetList.forEach(s -> {
                    if (!newestList.contains(s) && !newestList.contains(trimRight(s))) {
                        newestList.add(s);
                        log.append(s);
                        log.append("\n");
                    }
                });
            }
        }
        FileWriter fileWriter = new FileWriter(targetFile);
        BufferedWriter bw = new BufferedWriter(fileWriter);
        for (String s : newestList) {
            bw.write(s + "\n");
        }
        bw.close();
        targetReader.close();
        newestReader.close();
    }

    /**
     * 去右空格
     * @param str str
     * @return 去右空格
     */
    public static String trimRight(String str) {
        if (str == null || "".equals(str)) {
            return str;
        } else {
            return str.replaceAll("[　 ]+$", "");
        }
    }

    /**
     * 去左空格
     * @param str str
     * @return 去左空格
     */
    private static String trimLeft(String str) {
        if (str == null || "".equals(str)) {
            return str;
        } else {
            return str.replaceAll("^[　 ]+", "");
        }
    }

    /**
     * 文件调整-增删改
     * @param path 文件路径
     * @param addStrList 新增
     * @param removeStrList 删除
     * @param replaceStrMap 替换
     */
    public static void fileUpdateByStr(String path, List<String> addStrList, List<String> removeStrList, Map<String, String> replaceStrMap) throws IOException {
        // 读文件
        StringBuilder sb = readerFile(path);
        // 从构建器中生成字符串，并替换搜索文本
        for (String addStr : addStrList) {
            sb.append(addStr).append("\n");
        }
        for (String removeStr : removeStrList) {
            replaceStrMap.put(removeStr, "");
        }
        String str = sb.toString();
        for (Map.Entry<String, String> map : replaceStrMap.entrySet()) {
            str = str.replace(map.getKey(), map.getValue());
        }
        FileWriter flout = new FileWriter(path);
        // 把替换完成的字符串写入文件内
        flout.write(str.toCharArray());
        flout.close();
    }

    /**
     * 文件替换
     * @param localFilePath 源文件路径
     * @param fileFullPath 目标文件路径
     */
    public static void fileReplace(String localFilePath, String fileFullPath) {
        try {
            StringBuilder sb = readerFile(localFilePath);
            // 从构建器中生成字符串，并替换搜索文本
            String str = sb.toString();
            FileWriter flout = new FileWriter(fileFullPath);
            // 把替换完成的字符串写入文件内
            flout.write(str.toCharArray());
            flout.close();
        } catch (IOException e) {
            logger.error("服务升级file-update.xml文件替换失败  >>>>>>> {}", e.getMessage());
        }
    }

    private static StringBuilder readerFile(String localFilePath) throws IOException {
        FileReader reader = new FileReader(localFilePath);
        char[] data = new char[1024];
        int rn;
        StringBuilder sb = new StringBuilder();
        while ((rn = reader.read(data)) > 0) {
            // 把数组转换成字符串
            String str = String.valueOf(data, 0, rn);
            sb.append(str);
        }
        reader.close();
        return sb;
    }
}
