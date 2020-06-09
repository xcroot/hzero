package org.hzero.generator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * description
 *
 * @author shuangfei.zhu@hand-china.com 2020/03/05 13:40
 */
@Component
@ConfigurationProperties(prefix = "hzero.file")
public class FileConfig {

    /**
     * 获取文件签名url的地址
     */
    private String tokenUrl;
    /**
     * 访问系统的access_token
     */
    private String accessToken;

    public String getTokenUrl() {
        return tokenUrl;
    }

    public FileConfig setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
        return this;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public FileConfig setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }
}
