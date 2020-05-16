package smartrecipe.service.test.stepdefs;

import cucumber.api.java8.En;
import io.cucumber.datatable.DataTable;
import smartrecipe.service.entity.TagEntity;

import java.util.List;

import static org.junit.Assert.assertNotNull;

public class TagsCreationSteps extends AbstractSteps implements En {


    public TagsCreationSteps() {

        Given("client want to create tags", (DataTable recipes) -> {
            testContext().reset();
            List<TagEntity> recipeList = recipes.asList(TagEntity.class);

            // First row of DataTable has the employee attributes hence calling get(0) method.
            super.testContext()
                    .setPayload(recipeList);

        });

        When("client saves tags", () -> {
            String url = "/sr/tag";
            List<TagEntity> tagsInput= (List<TagEntity>) testContext().getPayload();
            tagsInput.stream().forEach(e ->
                    executePostForObject(url, e, TagEntity.class));
        });

        Then("client check last tag created", () -> {
            TagEntity tag = (TagEntity) testContext().getResult();
            assertNotNull("Tag id not null", tag.getId());
            assertNotNull("Tag name not null", tag.getName());
            //assertThat("Tags list created empty", tags, is(not(empty())));
            //tags.stream().forEach(e -> assertNotNull("Tag id null", e.getId()));
        });


    }

}
