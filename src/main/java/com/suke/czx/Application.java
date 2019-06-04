package com.suke.czx;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

//slf4j和swagger的注解
@Slf4j
@EnableSwagger2
@SpringBootApplication
public class Application {
//启动nginx,启动redis,输入localhost,admin/admin
//localhost:8077
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		log.info("==================X-SpringBoot启动成功================");
	}
}
