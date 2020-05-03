package smartrecipe.webgui.view;

import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;
import smartrecipe.webgui.dto.RecipeBinaryEntity;
import smartrecipe.webgui.dto.RecipeEntity;
import smartrecipe.webgui.dto.RecipeLight;
import smartrecipe.webgui.dto.TagEntity;
import smartrecipe.webgui.service.RecipeAPIClient;
import smartrecipe.webgui.service.TagAPIClient;
import smartrecipe.webgui.util.ImageUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * A simple example to introduce building forms. As your real application is probably much
 * more complicated than this example, you could re-use this form in multiple places. This
 * example component is only used in smartrecipe.webgui.view.MainView.
 * <p>
 * In a real world application you'll most likely using a common super class for all your
 * forms - less code, better UX.
 */
@SpringComponent
@UIScope
@Slf4j
public class RecipeEditor extends VerticalLayout implements KeyNotifier {

    //model
    private LinkedList<RecipeLight> allRecipes;
    //model current edited recipe
    private RecipeEntity recipe;
    private RecipeLight recipeLight;
    private ChangeHandler changeHandler;

    Binder<RecipeEntity> binder = new Binder<>(RecipeEntity.class);

    //text fields,combo, labels
    TextField name = new TextField("name");
    TextArea description = new TextArea("description");
    TextField webUrl = new TextField("Web URL");
    Anchor webUrlAnchor = new Anchor();
    TextField comment = new TextField("comment");
    TextField tagsListField = new TextField();
    ComboBox<TagEntity> tagComboBox = new ComboBox<>();
    TextField ingredientsField = new TextField("Ingredients");
    //Label webUrlLink = new Label("Recipe link");

    //buttons
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

    //image
    Image image = new Image();

    //services
    private RecipeAPIClient recipeAPIClient;
    private TagAPIClient tagAPIClient;


    public RecipeEditor(RecipeAPIClient recipeAPIClient, TagAPIClient tagAPIClient) {
        this.recipeAPIClient = recipeAPIClient;
        this.tagAPIClient = tagAPIClient;

        //configure sizes
        name.setWidth("1000px");
        description.setWidth("1000px");
        description.setHeight("150px");
        comment.setWidth("500px");
        webUrl.setWidth("500px");
        tagsListField.setWidth("1000px");
        ingredientsField.setWidth("1500px");

        //configure additionnal text
        webUrlAnchor.setText("GO to URL");
        webUrlAnchor.setTitle("Go to recipe page");
        //webUrlAnchor.add(webUrl);


        // bind using naming convention
        binder.bindInstanceFields(this);
        tagComboBox.setItems(tagAPIClient.findAll());

        // Configure and style components
        setSpacing(true);

        save.getElement().getThemeList().add("primary");
        delete.getElement().getThemeList().add("error");

        // wire action buttons to save, delete and reset
        //addKeyPressListener(Key.ENTER, e -> saveSimple());
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

        //layouts
        HorizontalLayout tagElements = new HorizontalLayout(tagComboBox, addTagBtn, removeTagBtn, tagsListField);
        HorizontalLayout imageElements = new HorizontalLayout(previousRecipeBtn, image, nextRecipeBtn);
        HorizontalLayout actions = new HorizontalLayout(save, cancel, delete, upload, rotateClockwise, rotateReverseClockwise);
        HorizontalLayout commentUrlLayout = new HorizontalLayout();
        commentUrlLayout.add(webUrl, comment);

        //add all elements to GUI
        add(name, description, commentUrlLayout, webUrlAnchor, tagElements, actions, imageElements);


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
        webUrlAnchor.setHref(recipe.getWebUrl());
        webUrlAnchor.setTarget("_blank");
        /*if (recipe.getWebUrl() != null) {
            webUrlLink.getElement().setProperty("innerHTML", "<a href=\"" + recipe.getWebUrl()
                    + "\" target=\"_blank\" style=\"target-new: tab ! important;\">" +
                    "Go to URL" + "</a>");
        }*/
        //manage tags
        if (recipe.getTags() != null) {
            String tags = "";
            for (TagEntity tagEntity : recipe.getTags()) {
                tags += " / " + tagEntity.getName().trim();
            }
            tagsListField.setValue(tags);
        }

        //manage image
        image.removeAll();
        image.setVisible(false);
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

        // Focus name initially
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