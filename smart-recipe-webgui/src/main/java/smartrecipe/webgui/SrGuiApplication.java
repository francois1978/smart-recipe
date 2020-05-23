package smartrecipe.webgui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class SrGuiApplication {
/*
    //HTTP port
    @Value("${http.port}")
    private int httpPort;*/


    public static void main(String[] args) {
        SpringApplication.run(SrGuiApplication.class, args);
    }
/*
    // Let's configure additional connector to enable support for both HTTP and HTTPS
    @Bean
    public ServletWebServerFactory servletContainer() {
        if (httpPort != 0) {
            TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
            tomcat.addAdditionalTomcatConnectors(createStandardConnector());
            return tomcat;
        }
        return new TomcatServletWebServerFactory();
    }

    private Connector createStandardConnector() {

        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setPort(httpPort);
        return connector;

    }*/
}

