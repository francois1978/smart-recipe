package smartrecipe.webgui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Route
//@Service
public class MainView extends VerticalLayout {


    private Grid<RecipeEntity> grid = null;


    private RecipeAPIClient recipeAPIClient;


    private RecipeEditor recipeEditor;
    private final Button addNewBtn;



    @Autowired
    public MainView(RecipeAPIClient recipeAPIClient) {

        this.recipeAPIClient = recipeAPIClient;
        this.recipeEditor =  new RecipeEditor(recipeAPIClient);
        this.addNewBtn = new Button("New recipe", VaadinIcon.PLUS.create());
// build layout


        TextField filter = new TextField();
        filter.setPlaceholder("Filter by last name");
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        filter.addValueChangeListener(e -> listRecipes(e.getValue()));

//configure grid
        this.grid = new Grid<>(RecipeEntity.class);
        Grid.Column nameCol = grid.getColumnByKey("name");
        Grid.Column desCol = grid.getColumnByKey("description");
        Grid.Column idCol = grid.getColumnByKey("id");
        Grid.Column binDesCol = grid.getColumnByKey("binaryDescription");
        //idCol.setVisible(false);
        binDesCol.setVisible(false);
        grid.setHeight("300px");
        grid.getColumnByKey("id").setWidth("50px").setFlexGrow(0);
        grid.setColumns("id", "name", "description");

        HorizontalLayout actions = new HorizontalLayout(filter, addNewBtn);
        add(actions, grid, recipeEditor);

        // Connect selected Customer to editor or hide if none is selected
        grid.asSingleSelect().addValueChangeListener(e -> {
            recipeEditor.editRecipe(e.getValue());
        });

        // Instantiate and edit new Customer the new button is clicked
        addNewBtn.addClickListener(e -> recipeEditor.editRecipe(new RecipeEntity()));

        // Listen changes made by the editor, refresh data from backend
        recipeEditor.setChangeHandler(() -> {
            recipeEditor.setVisible(false);
            listRecipes(filter.getValue());
        });

        // Initialize listing
        listRecipes(null);
    }

    void listRecipes(String filterText) {
        if (StringUtils.isEmpty(filterText)) {
            grid.setItems(recipeAPIClient.findAllRecipes());
        } else {
            grid.setItems(recipeAPIClient.findByKeyWord(filterText));
        }
    }

}