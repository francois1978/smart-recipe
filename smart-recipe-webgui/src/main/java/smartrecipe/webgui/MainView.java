package smartrecipe.webgui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Route
//@Service
public class MainView extends VerticalLayout {


    private Grid<RecipeLight> grid = null;
    private RecipeAPIClient recipeAPIClient;
    private RecipeEditor recipeEditor;
    private final Button addNewBtn = new Button("New recipe", VaadinIcon.PLUS.create());
    private final Button addTagBtn = new Button("Add tag", VaadinIcon.ADD_DOCK.create());
    private final Button removeTagBtn = new Button("Remove tag", VaadinIcon.TRASH.create());
    private final Button resetRecipeIngredientsBtn = new Button("Reset ingredient shopping list", VaadinIcon.ROTATE_LEFT.create());

    private final TextField descriptionFilterField = new TextField();
    private final TextField tagsList = new TextField();
    private final Set<TagEntity> tagEntityList = new HashSet<>();
    private TagAPIClient tagAPIClient;


    @Autowired
    public MainView(RecipeAPIClient recipeAPIClient, TagAPIClient tagAPIClient) {

        this.recipeAPIClient = recipeAPIClient;
        this.tagAPIClient = tagAPIClient;
        this.recipeEditor = new RecipeEditor(recipeAPIClient, tagAPIClient);

        //tag descriptionFilterField
        ComboBox<TagEntity> filterTagsCombo = new ComboBox<>();
        filterTagsCombo.setItems(tagAPIClient.findAll());
        addTagBtn.addClickListener(e -> onAddTag(filterTagsCombo.getValue()));
        removeTagBtn.addClickListener(e -> onRemoveTag());

        //description descriptionFilterField
        descriptionFilterField.setPlaceholder("Filter by auto description");
        descriptionFilterField.setValueChangeMode(ValueChangeMode.ON_CHANGE);
        descriptionFilterField.addValueChangeListener(e -> listRecipes(e.getValue()));

        //configure grid
        this.grid = new Grid<>(RecipeLight.class);
        grid.setHeight("300px");
        grid.getColumnByKey("id").setWidth("50px").setFlexGrow(0);
        grid.setColumns("id", "name", "description", "tagAsString");

        //configure layout
        HorizontalLayout actions = new HorizontalLayout(descriptionFilterField, addNewBtn, resetRecipeIngredientsBtn);
        HorizontalLayout tagActions = new HorizontalLayout(filterTagsCombo, addTagBtn, removeTagBtn, tagsList);
        add(actions, tagActions, grid, recipeEditor);

        // Connect selected recipe to edit recipe manager
        grid.asSingleSelect().addValueChangeListener(e -> {
            recipeEditor.editRecipe(e.getValue());
        });

        // Instantiate and edit new recipe the new button is clicked
        addNewBtn.addClickListener(e -> recipeEditor.editRecipe(new RecipeLight()));

        //reset ingredient shopping list
        resetRecipeIngredientsBtn.addClickListener(e -> recipeAPIClient.resetIngredientList());

        // Listen changes made by the editor, refresh data from backend
        recipeEditor.setChangeHandler(() -> {
            recipeEditor.setVisible(false);
            listRecipes(descriptionFilterField.getValue());
        });

        // Initialize listing
        listRecipes(null);
    }

    void listRecipes(String filterText) {
        if (StringUtils.isEmpty(filterText)) {
            //grid.setItems(recipeAPIClient.findAllRecipes());
        } else {
            List<RecipeLight> recipes = recipeAPIClient.findByKeyWordFullTextSearch(filterText, tagEntityList);
            grid.setItems(recipes);
            recipeEditor.setAllRecipes(new LinkedList<>(recipes));
        }
    }

    private void onAddTag(TagEntity value) {
        String tagName = value.getName().trim();
        tagsList.setValue(tagsList.getValue() + " / " + tagName);
        tagEntityList.add(value);
        if (StringUtils.isNotEmpty(descriptionFilterField.getValue())) {
            listRecipes(descriptionFilterField.getValue());
        }
    }

    private void onRemoveTag() {
        tagsList.setValue("");
        tagEntityList.clear();
    }

}