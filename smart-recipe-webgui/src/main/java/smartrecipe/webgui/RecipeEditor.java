package smartrecipe.webgui;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartrecipe.webgui.util.ImageUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * A simple example to introduce building forms. As your real application is probably much
 * more complicated than this example, you could re-use this form in multiple places. This
 * example component is only used in smartrecipe.webgui.MainView.
 * <p>
 * In a real world application you'll most likely using a common super class for all your
 * forms - less code, better UX.
 */
@SpringComponent
@UIScope
public class RecipeEditor extends VerticalLayout implements KeyNotifier {

    private static final Logger log = LoggerFactory.getLogger(RecipeEditor.class);

    private LinkedList<RecipeLight> allRecipes;

    /* Fields to edit properties in Customer smartrecipe.service.entity */
    TextField name = new TextField("name");
    TextField description = new TextField("description");
    TextField comment = new TextField("comment");
    TextField tagsListField = new TextField();
    ComboBox<TagEntity> tagComboBox = new ComboBox<>();
    TextField ingredientsField = new TextField("Ingredients");
    //Image image = new Image("/recipe1.jpg", "Recipe image");


    /* Action buttons */
    Button addTagBtn = new Button("Add tag", VaadinIcon.ADD_DOCK.create());
    Button removeTagBtn = new Button("Remove tag", VaadinIcon.TRASH.create());

    Button save = new Button("Save", VaadinIcon.CHECK.create());
    Button cancel = new Button("Cancel");
    Button delete = new Button("Delete", VaadinIcon.TRASH.create());
    Button rotateClockwise = new Button("Rotate clockwise", VaadinIcon.ROTATE_RIGHT.create());
    Button rotateReverseClockwise = new Button("Rotate reverse clockwise", VaadinIcon.ROTATE_LEFT.create());
    Button addIngredientBtn = new Button("Get ingredient", VaadinIcon.ADD_DOCK.create());
    Upload upload = setUpUpload();
    Button nextRecipeBtn = new Button("", VaadinIcon.FORWARD.create());
    Button previousRecipeBtn = new Button("", VaadinIcon.BACKWARDS.create());


    HorizontalLayout actions = new HorizontalLayout(save, cancel, delete, upload, addIngredientBtn, rotateClockwise, rotateReverseClockwise);
    Binder<RecipeEntity> binder = new Binder<>(RecipeEntity.class);

    //image
    Image image = new Image();

    //utils
    private RecipeAPIClient recipeAPIClient;
    private TagAPIClient tagAPIClient;
    /**
     * The currently edited recipe
     */
    private RecipeEntity recipe;
    private RecipeLight recipeLight;
    private ChangeHandler changeHandler;


    public RecipeEditor(RecipeAPIClient recipeAPIClient, TagAPIClient tagAPIClient) {
        this.recipeAPIClient = recipeAPIClient;
        this.tagAPIClient = tagAPIClient;
        name.setWidth("1000px");
        description.setWidth("1000px");
        comment.setWidth("1000px");
        tagsListField.setWidth("1000px");
        ingredientsField.setWidth("1500px");
        //image.setWidth("1000px");
        //image.setHeight("2000px");
        // bind using naming convention
        binder.bindInstanceFields(this);

        tagComboBox.setItems(tagAPIClient.findAll());

        // Configure and style components
        setSpacing(true);

        save.getElement().getThemeList().add("primary");
        delete.getElement().getThemeList().add("error");

        addKeyPressListener(Key.ENTER, e -> saveSimple());

        // wire action buttons to save, delete and reset
        addTagBtn.addClickListener(e -> onAddTag(tagComboBox.getValue()));
        removeTagBtn.addClickListener(e -> onRemoveTag());
        addIngredientBtn.addClickListener(e -> addIngredientToShoppingList());


        save.addClickListener(e -> saveSimple());
        delete.addClickListener(e -> delete());
        cancel.addClickListener(e -> editRecipe(recipeLight));
        rotateClockwise.addClickListener(e -> rotate(90));
        rotateReverseClockwise.addClickListener(e -> rotate(-90));
        nextRecipeBtn.addClickListener(e -> nextRecipe());
        previousRecipeBtn.addClickListener(e -> previousRecipe());

        setVisible(false);

        //configure upload button

        //add all elements to GUI
        HorizontalLayout tagElements = new HorizontalLayout(tagComboBox, addTagBtn, removeTagBtn, tagsListField);
        HorizontalLayout imageElements = new HorizontalLayout(previousRecipeBtn, image, nextRecipeBtn);

        add(name, description, comment, tagElements, actions, ingredientsField, imageElements);


    }

    private void addIngredientToShoppingList() {
        java.util.List<String> ingredientList = recipeAPIClient.addIngredientToShoppingList(recipe.getId());
        ingredientsField.setValue("");
        for (String ingredient : ingredientList) {
            ingredientsField.setValue(ingredientsField.getValue() + " / " + ingredient);
        }
    }

    private void onAddTag(TagEntity value) {
        if (recipe.getTags() == null || recipe.getTags().contains(value)) {
            return;
        }
        String tagName = value.getName().trim();
        tagsListField.setValue(tagsListField.getValue() + " / " + tagName);
        if (recipe.getTags() == null) {
            recipe.setTags(new HashSet<>());
        }
        recipe.getTags().add(value);
    }

    private void onRemoveTag() {

        tagsListField.setValue("");
        if (recipe.getTags() != null) {
            recipe.getTags().clear();
        }
    }


    private Upload setUpUpload() {
        MemoryBuffer memoryBuffer = new MemoryBuffer();
        Upload upload = new Upload(memoryBuffer);
        upload.setAutoUpload(true);
        upload.setUploadButton(new Button("Upload"));

        upload.addSucceededListener(event -> {
            log.info("File received: " + event.getFileName() + " Size: " + memoryBuffer.getFileData().getMimeType());
            byte[] targetArray = new byte[0];
            try {
                targetArray = new byte[memoryBuffer.getInputStream().available()];
                memoryBuffer.getInputStream().read(targetArray);
            } catch (IOException e) {
                log.error("Error while processing image file uploaded", e);
            }
            RecipeBinaryEntity recipeBinaryEntity = new RecipeBinaryEntity();
            recipeBinaryEntity.setBinaryDescription(targetArray);
            recipe.setRecipeBinaryEntity(recipeBinaryEntity);
            saveOcr();
        });
        return upload;
    }

    void rotate(double angle) {
        try {
            byte[] rotateImage = ImageUtils.rotateImage(recipe.getRecipeBinaryEntity().getBinaryDescription(), angle);
            recipe.getRecipeBinaryEntity().setBinaryDescription(rotateImage);
            saveSimple();
        } catch (IOException e) {
            log.error("Error rotating image", e);
        }

    }

    void nextRecipe() {
        if (allRecipes.getLast().equals(recipeLight)) {
            editRecipe(allRecipes.getFirst());
        } else {
            int currentRecipeIndex = allRecipes.indexOf(recipeLight);
            editRecipe(allRecipes.get(currentRecipeIndex + 1));
        }
    }

    void previousRecipe() {
        if (allRecipes.getFirst().equals(recipeLight)) {
            editRecipe(allRecipes.getLast());
        } else {
            int currentRecipeIndex = allRecipes.indexOf(recipeLight);
            editRecipe(allRecipes.get(currentRecipeIndex - 1));
        }
    }

    void delete() {
        recipeAPIClient.deleteById(recipe.getId());
        changeHandler.onChange();
    }

    void saveSimple() {
        recipeAPIClient.saveRecipeSimple(recipe);
        changeHandler.onChange();
    }

    void saveOcr() {
        recipeAPIClient.saveRecipeOcr(recipe);
        changeHandler.onChange();
    }

    public final void editRecipe(RecipeLight recipeLight) {
        if (recipeLight == null) {
            setVisible(false);
            return;
        }
        this.recipeLight = recipeLight;
        final boolean persisted = recipeLight.getId() != null;
        if (persisted) {
            // Find fresh smartrecipe.service.entity for editing
            this.recipe = recipeAPIClient.findRecipeById(recipeLight.getId());
        } else {

            this.recipe = new RecipeEntity();
        }
        cancel.setVisible(persisted);

        // Bind recipe properties to similarly named fields
        // Could also use annotation or "manual binding" or programmatically
        // moving values from fields to entities before saving
        binder.setBean(this.recipe);

        //manage tags
        if (recipe.getTags() != null) {
            String tags = "";
            for (TagEntity tagEntity : recipe.getTags()) {
                tags += " / " + tagEntity.getName().trim();
            }
            tagsListField.setValue(tags);
        }
        if (recipe.getRecipeBinaryEntity() != null && recipe.getRecipeBinaryEntity().getBinaryDescription() != null) {

            try {
                byte[] imageScaled = ImageUtils.geScaledImage(recipe.getRecipeBinaryEntity().getBinaryDescription(), 1200);
                StreamResource resource = new StreamResource("image.jpg ",
                        () -> new ByteArrayInputStream(imageScaled));
                image.setSrc(resource);
                image.setVisible(true);

            } catch (IOException e) {
                log.error("Unable to process image", e);
            }
        }

        setVisible(true);

        // Focus first name initially
        name.focus();

    }

    public void setChangeHandler(ChangeHandler h) {
        // ChangeHandler is notified when either save or delete
        // is clicked
        changeHandler = h;
    }

    public interface ChangeHandler {
        void onChange();
    }

    public void setAllRecipes(LinkedList<RecipeLight> allRecipes) {
        this.allRecipes = allRecipes;
    }
}