package org.hzero.generator.liquibase;

import javax.sql.DataSource;
import org.hzero.generator.liquibase.metadata.impl.MetadataDriverDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by hailuoliu@choerodon.io on 2018/7/11.
 */
public class StartupRunner {
    @Value("${spring.h2.console.enabled:false}")
    boolean h2Console;
    @Value("${metadata.init:false}")
    boolean metadataInit;
    @Autowired
    LiquibaseExecutor liquibaseExecutor;
    @Autowired
    DataSource dataSource;


    public void test(String... args) throws Exception {
        boolean success = liquibaseExecutor.execute(args);
        if (success && metadataInit) {
            MetadataDriverDelegate.syncMetadata(dataSource);
        }
        if (!h2Console) {
            if (success) {
                System.exit(0);
            } else {
                System.exit(1);
            }
        }
    }
}
