package smartrecipe.service.test.stepdefs;

import io.cucumber.datatable.DataTable;
import cucumber.api.java8.En;
import io.restassured.response.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class RecipeSteps extends AbstractSteps implements En {

    public RecipeSteps() {

        Given("client wants to health check service", (DataTable input) -> {
                    testContext().reset();
                }
        );

        When("the client calls the end point to check", (String testContext) -> {
            String hcUrl = "/healthcheck";

            // AbstractSteps class makes the POST call and stores response in TestContext
            executeGet(hcUrl);
        });

        Then("the client receives response {string}", (String expectedResult) -> {
            Response response = testContext().getResponse();
            assertThat(response.getBody() != null);
        });

    }

}
