package com.fiberhome;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @file: com.nuts.demo.NutsDevApplication
 * @version: 1.0.0
 * @Description: 主启动类
 * @Author: X7515
 * @Date: 2021/9/22 14:56
 *
 * 注意事项：
 *  1、使用jsp模板引擎时 多模块时开发工具中配置当前项目地址 Working director: $MODULE_WORKING_DIR$
 *  2、使用war包部署时application.properties中下面两个配置失效
 *     server.port=
 *     server.servlet.context-path=
 */

@SpringBootApplication
@ComponentScan(basePackages = {"com.nuts","com.fiberhome"})
@EnableScheduling
public class FileManagerApplication extends SpringBootServletInitializer {

    private static final Logger log = LoggerFactory.getLogger(FileManagerApplication.class);

    /**
     * 主启动类
     * @param args 参数
     */
    public static void main(String[] args) {
        SpringApplication.run(FileManagerApplication.class, args);
        log.info("启动成功！");
        //log.info("启动成功！首页地址:{}", SystemProperties.getNutsHome());
        //if(!ContextUtils.isStartWithJar(CapMatrixApplication.class)) {
        //    log.warn("有问题可以查看脚手架开发文档使用浏览器打开: {}", devDocument());
        //}
    }

    /**
     * @return 脚手架开发文档路径
     */
    private static String devDocument(){
        return System.getProperty("user.dir") + File.separator + "doc" + File.separator + "index.html";
    }

}

