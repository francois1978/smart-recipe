package smartrecipe.webgui;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;

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

    RecipeAPIClient apiClient = new RecipeAPIClient();


    /**
     * The currently edited recipe
     */
    private RecipeEntity recipe;

    /* Fields to edit properties in Customer smartrecipe.service.entity */
    TextField name = new TextField("name");
    TextField description = new TextField("description");

    /* Action buttons */
    // TODO why more code?
    Button save = new Button("Save", VaadinIcon.CHECK.create());
    Button cancel = new Button("Cancel");
    Button delete = new Button("Delete", VaadinIcon.TRASH.create());
    HorizontalLayout actions = new HorizontalLayout(save, cancel, delete);

    Binder<RecipeEntity> binder = new Binder<>(RecipeEntity.class);
    private ChangeHandler changeHandler;

    @Autowired
    public RecipeEditor() {
        this.apiClient = new RecipeAPIClient();
        name.setWidth("1000px");
        description.setWidth("1000px");


        // bind using naming convention
        binder.bindInstanceFields(this);

        // Configure and style components
        setSpacing(true);

        save.getElement().getThemeList().add("primary");
        delete.getElement().getThemeList().add("error");

        addKeyPressListener(Key.ENTER, e -> save());

        // wire action buttons to save, delete and reset
        save.addClickListener(e -> save());
        delete.addClickListener(e -> delete());
        cancel.addClickListener(e -> editRecipe(recipe));
        setVisible(false);



        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif");
/*
        upload.addSucceededListener(event -> {
            Component component = create(event.getMIMEType(),
                    event.getFileName(),
                    buffer.getInputStream(event.getFileName()));
            showOutput(event.getFileName(), component, output);
        });*/

        add(name, description, actions, upload);
    }

    void delete() {
        //smartrecipe.service.repository.delete(recipe);
        changeHandler.onChange();
    }

    void save() {
        apiClient.saveRecipe(recipe);
        changeHandler.onChange();
    }

    public interface ChangeHandler {
        void onChange();
    }

    public final void editRecipe(RecipeEntity recipe) {
        if (recipe == null) {
            setVisible(false);
            return;
        }
        final boolean persisted = recipe.getId() != null;
        if (persisted) {
            // Find fresh smartrecipe.service.entity for editing
            this.recipe = apiClient.findRecipeById(recipe.getId());
        } else {
            this.recipe = recipe;
        }
        cancel.setVisible(persisted);

        // Bind recipe properties to similarly named fields
        // Could also use annotation or "manual binding" or programmatically
        // moving values from fields to entities before saving
        binder.setBean(this.recipe);

        setVisible(true);

        // Focus first name initially
        name.focus();
    }

    public void setChangeHandler(ChangeHandler h) {
        // ChangeHandler is notified when either save or delete
        // is clicked
        changeHandler = h;
    }


}