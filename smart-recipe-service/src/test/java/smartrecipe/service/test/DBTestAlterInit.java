package smartrecipe.service.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Configuration
@Profile("test")
@Slf4j
public class DBTestAlterInit {


    private final String SAMPLE_DATA = "classpath:h2_alter.sql";

    @Autowired
    private DataSource datasource;

    @PostConstruct
    public void loadIfInMemory() throws Exception {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(SAMPLE_DATA);
        ScriptUtils.executeSqlScript(datasource.getConnection(), resource);
        log.info("Script executed to alter H2 DB");
    }

}
