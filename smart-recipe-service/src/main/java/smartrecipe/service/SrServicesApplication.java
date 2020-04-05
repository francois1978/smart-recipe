package smartrecipe.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import smartrecipe.service.entity.RecipeEntity;
import smartrecipe.service.repository.RecipeRepository;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@SpringBootApplication
@EnableJpaAuditing
@EnableSwagger2
@ComponentScan(basePackages = {"smartrecipe.service.sensor.controller","smartrecipe.service.sensor.service","smartrecipe.service.helper","smartrecipe.service.controller","smartrecipe.service.ocr"})
public class SrServicesApplication {

    private static final Logger log = LoggerFactory.getLogger(SrServicesApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SrServicesApplication.class, args);
    }

    //@Bean
    public CommandLineRunner demo(RecipeRepository repository) {
        return (args) -> {
            repository.save(new RecipeEntity(null, "test des", "test name"));
            log.info("-------------------------------");
            for (RecipeEntity recipe : repository.findAll()) {
                log.info(recipe.toString());
            }
            log.info("");
        };
    }
}

