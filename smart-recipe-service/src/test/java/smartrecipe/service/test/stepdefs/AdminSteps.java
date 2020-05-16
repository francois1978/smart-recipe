package smartrecipe.service.test.stepdefs;

import cucumber.api.java8.En;


public class AdminSteps extends AbstractSteps implements En {


    private String expectedMergedImageCheckSum;

    public AdminSteps() {

        Given("client wants to rebuild lucene indexes",
                () -> {
                    testContext().reset();
                });


        When("client run rebuild lucene indexes", () -> {
            String url = "/sr/buildluceneindex/";
            executeGet(url);
        });

        Then("client is happy to have lucene indexes rebuilt", () -> {
        });


    }
}
