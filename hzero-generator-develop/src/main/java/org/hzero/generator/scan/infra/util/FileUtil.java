package org.hzero.generator.scan.infra.util;

import org.apache.commons.io.FileUtils;
import org.hzero.generator.scan.domain.UiComponent;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件处理
 *
 * @author fanghan.liu 2020/01/15 20:34
 */
public class FileUtil {

    /**
     * 将文件转为字符串并去除空格
     *
     * @param file 文件
     * @return 文件内容
     */
    public static String fileToStringWithoutSpace(File file) {
        String fileContent;
        try {
            fileContent = FileUtils.readFileToString(file);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        List<UiComponent> uiComponents = new ArrayList<>();
        fileContent = StringUtils.trimAllWhitespace(fileContent);
        fileContent = StringUtils.replace(fileContent, "`", "");
        return fileContent;
    }

}
