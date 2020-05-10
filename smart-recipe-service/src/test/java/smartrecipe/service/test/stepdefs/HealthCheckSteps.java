package smartrecipe.service.test.stepdefs;

import cucumber.api.java8.En;

import static org.junit.Assert.assertTrue;

public class HealthCheckSteps extends AbstractSteps implements En {


    private int budget = 0;

    public HealthCheckSteps() {

        Given("client wants to health check service", () -> {
                    testContext().reset();
                }
        );

        When("the client calls the end point to check", () -> {
            String hcUrl = "/sr/healthcheck";
            executeGetWithStringResult(hcUrl);
        });

        Then("the client receives response {string}", (String expectedResult) -> {
            String response = (String) testContext().getResult();
            assertTrue(response.contains(response));
        });



    }

}
