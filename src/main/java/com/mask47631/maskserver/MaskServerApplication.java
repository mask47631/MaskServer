package com.mask47631.maskserver;

import com.mask47631.maskserver.entity.User;
import com.mask47631.maskserver.repository.UserRepository;
import com.mask47631.maskserver.scheduled.WebVersionScheduledTask;
import com.mask47631.maskserver.util.PasswordUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.FileWriter;
import java.io.IOException;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class MaskServerApplication {

    @Schema(description = "自动获取最新前端版本")
    @Value("${app.autoGetWeb:false}")
    private boolean autoGetWeb;

    public static void main(String[] args) {
        SpringApplication.run(MaskServerApplication.class, args);
    }

    @Bean
    public CommandLineRunner adminUserInitializer(UserRepository userRepository) {
        return args -> {
            if (userRepository.findFirstByRole("admin").isEmpty()) {
                String password = PasswordUtil.generateRandomPassword(10);
                String md5Password = PasswordUtil.md5(password);
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@admin");
                admin.setRole("admin");
                admin.setVerified(false);
                admin.setDisabled(false);
                admin.setPassword(md5Password);
                userRepository.save(admin);
                try (FileWriter writer = new FileWriter("admin.log", false)) {
                    writer.write("username: admin@admin"+ System.lineSeparator()+"password: " + password + System.lineSeparator());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (autoGetWeb){
                WebVersionScheduledTask.latestWebVersion();
            }
        };
    }
}