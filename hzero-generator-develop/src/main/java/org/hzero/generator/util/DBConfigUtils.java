package org.hzero.generator.util;

import java.util.Map;
import org.hzero.generator.constant.Constant;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 数据源配置获取工具
 *
 * @author wanshun.zhang@hand-china.com
 * @date 2019/11/08 16:55
 */
@Component
@ConfigurationProperties(prefix = "spring.datasource.dynamic.datasource")
public class DBConfigUtils {

    private Map<String, String> gen;
    private Map<String, String> dev;
    private Map<String, String> tst;
    private Map<String, String> uat;
    private Map<String, String> prd;

    public Map<String, String> getMapByEnv(String env) {
        switch (env) {
            case Constant.ENV_DEV:
                return getDev();
            case Constant.ENV_TST:
                return getTst();
            case Constant.ENV_UAT:
                return getUat();
            case Constant.ENV_PRD:
                return getPrd();
            default:
                return getGen();
        }
    }

    public Map<String, String> getGen() {
        return gen;
    }

    public void setGen(Map<String, String> gen) {
        this.gen = gen;
    }

    public Map<String, String> getDev() {
        return dev;
    }

    public void setDev(Map<String, String> dev) {
        this.dev = dev;
    }

    public Map<String, String> getTst() {
        return tst;
    }

    public void setTst(Map<String, String> tst) {
        this.tst = tst;
    }

    public Map<String, String> getUat() {
        return uat;
    }

    public void setUat(Map<String, String> uat) {
        this.uat = uat;
    }

    public Map<String, String> getPrd() {
        return prd;
    }

    public void setPrd(Map<String, String> prd) {
        this.prd = prd;
    }
}
