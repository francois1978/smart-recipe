package smartrecipe.service.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
@EnableScheduling
public class SpringConfig {

    @PostConstruct
    private void configLoaded() {
        log.info("Configuration loaded");
    }

}