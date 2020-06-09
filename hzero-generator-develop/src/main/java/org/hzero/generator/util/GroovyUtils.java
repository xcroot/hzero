package org.hzero.generator.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * @Description 修改groovy脚本文件，适配pgsql数据库
 * @Date 2020-04-02 18:56
 * @Author wanshun.zhang@hand-china.com
 */
public class GroovyUtils {

    private static List<String> changeResult = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        // groovy脚本目录
        String dir = "D:\\workspace\\WorkDev\\hzero-resource\\groovy";
        // 处理结果记录文件路径
        String result = "C:\\Users\\Andy\\Desktop\\result.txt";
        File file = new File(dir);
        handleFile(file);
        writeResult(result);
        System.out.println(changeResult.size());
    }

    private static void writeResult(String result) {
        File file = new File(result);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (String log : changeResult) {
                bw.write(log);
                bw.newLine();
            }
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleFile(File file) throws IOException {
        //处理目录情况
        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            if (subFiles != null) {
                for (File subFile : subFiles) {
                    handleFile(subFile);
                }
            }
        } else {
            String filename = file.getName();
            if (filename.endsWith(".groovy")) {
                // 修改导入
                fileChange(file);
            }
        }
    }

    private static void fileChange(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            List<String> list = new ArrayList<>();
            changeResult.add(file.getName());
            String line;
            while ((line = br.readLine()) != null) {
                boolean flag = false;
                String temp = line;
                // unsigned
                if (StringUtils.contains(line, "unsigned") || StringUtils.contains(line, "UNSIGNED")){
                    line = line.replace(" unsigned", "");
                    line = line.replace(" UNSIGNED", "");
                    flag = true;
                }
                // bigint、tinyint、int
                if (StringUtils.contains(line.toUpperCase(), "INT(")){
                    line = line.replaceFirst("INT\\([1-9]\\d*\\)","INT");
                    line = line.replaceFirst("int\\([1-9]\\d*\\)","int");
                    line = line.replaceFirst("Int\\([1-9]\\d*\\)","int");
                    flag = true;
                }
                list.add(line);
                if (flag){
                    changeResult.add(temp.trim()+"---"+line.trim());
                }

            }
            try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                for (int i = 0, size = list.size(); i < size; i++) {
                    osw.write(list.get(i));
                    if (i < size - 1) {
                        osw.write("\n");
                    }
                }
                osw.flush();
            }
        }
    }

}
