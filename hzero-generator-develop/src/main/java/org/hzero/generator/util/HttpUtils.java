package org.hzero.generator.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * http工具类
 *
 * @author shuangfei.zhu@hand-china.com 2020/03/05 15:59
 */
public class HttpUtils {
    private HttpUtils() {
    }

    private static Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    /**
     * 下载
     *
     * @param url 下载地址
     * @return 下载输入流
     */
    public static InputStream get(String url) throws IOException {
        Assert.notNull(url, "Url is not null");
        URL downUrl;
        downUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) downUrl.openConnection();
        // 设置超时间为10秒
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(10_000);
        return conn.getInputStream();
    }
}
