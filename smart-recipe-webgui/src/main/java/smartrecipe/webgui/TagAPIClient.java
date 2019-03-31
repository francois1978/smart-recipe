package smartrecipe.webgui;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Slf4j

@PropertySource("classpath:/application.properties")
@Configuration
@ComponentScan
@EnableAutoConfiguration

public class TagAPIClient {


    @Value("${service.url}")
    private String serviceUrl;

    public List<TagEntity> findAll() {
        //read all
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<TagEntity[]> response = restTemplate.getForEntity(
                serviceUrl + "tags  ", TagEntity[].class);

        List result =
                Arrays.asList(response.getBody());
                        //.stream().map(TagEntity::getName).collect(Collectors.toList());
        log.info("Number of total tagsListField: " + result.size());
        return result;
    }
}
