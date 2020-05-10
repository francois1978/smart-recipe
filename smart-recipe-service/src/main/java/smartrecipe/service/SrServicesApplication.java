package smartrecipe.service;

import org.apache.catalina.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import smartrecipe.service.entity.RecipeEntity;
import smartrecipe.service.repository.RecipeRepository;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@SpringBootApplication
@EnableJpaAuditing
@EnableSwagger2
@ComponentScan(basePackages = {"smartrecipe.service.test.servicemock","smartrecipe.service.security","smartrecipe.service.test","smartrecipe.service.sensor.controller","smartrecipe.service.sensor.service","smartrecipe.service.helper","smartrecipe.service.controller","smartrecipe.service.ocr"})
public class SrServicesApplication {

    private static final Logger log = LoggerFactory.getLogger(SrServicesApplication.class);

    //HTTP port
    @Value("${http.port}")
    private int httpPort;


    public static void main(String[] args) {
        SpringApplication.run(SrServicesApplication.class, args);
    }


    // Let's configure additional connector to enable support for both HTTP and HTTPS
    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addAdditionalTomcatConnectors(createStandardConnector());
        return tomcat;
    }

    private Connector createStandardConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setPort(httpPort);
        return connector;
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

