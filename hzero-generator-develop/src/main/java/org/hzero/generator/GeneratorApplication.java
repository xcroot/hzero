package org.hzero.generator;

import org.hzero.generator.util.XmlUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.net.InetAddress;

/**
 * HZero代码生成器
 *
 * @author xianzhi.chen@hand-china.com	2018年6月19日下午2:16:50
 * @version 1.0
 * @name org.hzero.generator.GeneratorApplication
 * @description
 */
@SpringBootApplication
@EnableConfigurationProperties
public class GeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeneratorApplication.class, args);
        try {
            // 解析xml文件
            XmlUtils.resolver();
            System.out.println("========================================================================================");
            System.out.println("Help document: http://hzerodoc.saas.hand-china.com/zh/docs/development-guide/generator/");
            System.out.println("The project address: http://" + InetAddress.getLocalHost().getHostAddress() + ":8090");
            System.out.println("========================================================================================");
        } catch (Throwable e) {
            e.getMessage();
        }

    }
}
