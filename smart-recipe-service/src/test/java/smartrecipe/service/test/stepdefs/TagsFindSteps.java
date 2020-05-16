package smartrecipe.service.test.stepdefs;

import cucumber.api.java8.En;
import smartrecipe.service.entity.TagEntity;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TagsFindSteps extends AbstractSteps implements En {


    public TagsFindSteps() {

        Given("client wants to get all tags", () -> {
            testContext().reset();
        });

        When("client loads all tags", () -> {
            String url = "/sr/tags";
            executeGetWithListResult(url, TagEntity[].class);
        });

        Then("client receives all tags, count expected {string}", (String tagCountExpected) -> {
            List<TagEntity> tags = (List<TagEntity>) testContext().getResultList();
            assertThat("Tags list created empty", tags, is(not(empty())));
            assertTrue("Tags count not equals to " + tagCountExpected, tags.size() == Integer.parseInt(tagCountExpected));
            tags.stream().forEach(e -> assertNotNull("Tag id null", e.getId()));
            tags.stream().forEach(e -> assertNotNull("Tag name null", e.getName()));
        });


    }

}
