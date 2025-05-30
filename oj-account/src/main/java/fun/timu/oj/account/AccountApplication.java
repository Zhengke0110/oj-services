package fun.timu.oj.account;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@MapperScan("fun.timu.oj.account.mapper")
@EnableTransactionManagement
@EnableFeignClients
@EnableDiscoveryClient
@ComponentScan(basePackages = {"fun.timu.oj.account", "fun.timu.oj.common"})
@SpringBootApplication
public class AccountApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountApplication.class, args);
    }


}
