package fun.timu.oj.shandbox.docker;

import fun.timu.oj.shandbox.docker.template.DockerSandboxExample;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
