package smartrecipe.service.test;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;

/**
 * To run cucumber test
 */
@RunWith(Cucumber.class)
@Slf4j
@CucumberOptions(features = "classpath:features", plugin = {"pretty",
                                                            "json:target/cucumber-report.json"})
public class CucumberTest {




}
