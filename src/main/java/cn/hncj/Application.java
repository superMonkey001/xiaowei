package cn.hncj;

import cn.hncj.utils.SpringUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "cn.hncj.mapper")
@ComponentScan(basePackages = {"cn.hncj","org.n3r.idworker"})
public class Application {

	@Bean
	public SpringUtil getSpringUtil() {
		return new SpringUtil();
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
