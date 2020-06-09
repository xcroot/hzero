package org.hzero.generator.export.helper.supporter;

import org.hzero.generator.export.helper.exception.LiquibaseHelperException;

import java.io.*;

/**
 * <p>
 * File Helper
 * </p>
 *
 * @author qingsheng.chen 2018/11/24 星期六 10:29
 */
public class FileHelper {
    private static final int BUF_SIZE = 2048;

    private FileHelper() {
    }

    public static void copyFile(String sourceFilePath, File targetFile) {
        copyFile(new File(sourceFilePath), targetFile);
    }

    public static void copyFile(File sourceFile, File targetFile) {
        try (
                FileInputStream fis = new FileInputStream(sourceFile);
                FileOutputStream fos = new FileOutputStream(targetFile)
        ) {
            byte[] buf = new byte[BUF_SIZE];
            while (fis.read(buf) != -1) {
                fos.write(buf);
            }
            fos.flush();
        } catch (IOException e) {
            throw new LiquibaseHelperException("文件复制失败!", e);
        }
    }

    public static void copyFile(InputStream is, File targetFile) {
        try (
                FileOutputStream fos = new FileOutputStream(targetFile)
        ) {
            byte[] buf = new byte[BUF_SIZE];
            while (is.read(buf) != -1) {
                fos.write(buf);
            }
            fos.flush();
        } catch (IOException e) {
            throw new LiquibaseHelperException("文件复制失败!", e);
        }
    }
}
