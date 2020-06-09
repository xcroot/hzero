package org.hzero.generator.util;

import java.io.File;
import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

public class CommandExecuter {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final static String OS_LINUX = "Linux";
    private final static String OS_WINDOWS = "Windows";
    private final static String WINDOWS_DIFF_CMD = "\\main.exe conf=diff";
    private final static String WINDOWS_UPDATE_CMD = "\\main.exe conf=update";
    private final static String LINUX_DIFF_CMD = "\\main conf=diff";
    private final static String LINUX_UPDATE_CMD = "\\main conf=update";
    private final static String osName = System.getProperties().getProperty("os.name");

    public static void main(String[] args) {
        CommandExecuter ce = new CommandExecuter();
        ce.runDatabaseDiff();
    }

    public boolean runDatabaseDiff() {
        String strCmd = "";
        File file = getRootFile();
        if (osName.contains(OS_LINUX)) {
            strCmd = file.getAbsolutePath() + LINUX_DIFF_CMD;
        } else if (osName.contains(OS_WINDOWS)) {
            strCmd = file.getAbsolutePath() + WINDOWS_DIFF_CMD;
        }
        // 执行命名
        return runCmd(strCmd, file);
    }

    public boolean runDatabaseUpdate() {
        String strCmd = "";
        File file = getRootFile();
        if (osName.contains(OS_LINUX)) {
            strCmd = file.getAbsolutePath() + LINUX_UPDATE_CMD;
        } else if (osName.contains(OS_WINDOWS)) {
            strCmd = file.getAbsolutePath() + WINDOWS_UPDATE_CMD;
        }
        // 执行命名
        return runCmd(strCmd, file);
    }

    public boolean runCmd(String strCmd, File file) {
        Runtime rt = Runtime.getRuntime(); // Runtime.getRuntime()返回当前应用程序的Runtime对象
        Process ps = null; // Process可以控制该子进程的执行或获取该子进程的信息。
        try {
            ps = rt.exec(strCmd, null, file); // 该对象的exec()方法指示Java虚拟机创建一个子进程执行指定的可执行程序，并返回与该子进程对应的Process对象实例。
            ps.waitFor(); // 等待子进程完成再往下执行。
        } catch (Exception e) {
            e.printStackTrace();
        }
        int i = ps.exitValue(); // 接收执行完毕的返回值
        ps.destroy(); // 销毁子进程
        ps = null;
        if (i == 0) {
            logger.info("执行完成.");
            return true;
        } else {
            logger.error("执行失败.");
            return true;
        }
    }

    private File getRootFile() {
        try {
            File file = new File(ResourceUtils.getURL("classpath:").getPath());
            if (!file.exists()) {
                file = new File("");
            }
            return file;
        } catch (FileNotFoundException e) {
            logger.error(e.getLocalizedMessage());
        }
        return null;
    }

}
