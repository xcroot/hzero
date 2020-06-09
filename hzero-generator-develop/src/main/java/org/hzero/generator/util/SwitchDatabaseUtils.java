package org.hzero.generator.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 切换数据库
 *
 * @author wanshun.zhang@hand-china.com
 * @date 2019/11/08 16:23
 */
@Component
public class SwitchDatabaseUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwitchDatabaseUtils.class);

    @Autowired
    JdbcTemplate jdbcTemplate;

    public void switchDatabase(String database) {
        Assert.notNull(database, "切换的数据库不能为 [null]！");
        jdbcTemplate.execute("use " + database);
    }

}
