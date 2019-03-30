package smartrecipe.webgui;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.button.Button;
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
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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


    /* Fields to edit properties in Customer smartrecipe.service.entity */
    TextField name = new TextField("name");
    TextField description = new TextField("description");
    TextField comment = new TextField("comment");
    //Image image = new Image("/recipe1.jpg", "Recipe image");


    /* Action buttons */
    Button save = new Button("Save", VaadinIcon.CHECK.create());
    Button cancel = new Button("Cancel");
    Button delete = new Button("Delete", VaadinIcon.TRASH.create());
    Button rotateClockwise = new Button("Rotate clockwise", VaadinIcon.ROTATE_RIGHT.create());
    Button rotateReverseClockwise = new Button("Rotate reverse clockwise", VaadinIcon.ROTATE_LEFT.create());
    HorizontalLayout actions = new HorizontalLayout(save, cancel, delete, rotateClockwise, rotateReverseClockwise);
    Binder<RecipeEntity> binder = new Binder<>(RecipeEntity.class);

    //image
    Image image = new Image();

    //utils
    private RecipeAPIClient recipeAPIClient;
    /**
     * The currently edited recipe
     */
    private RecipeEntity recipe;
    private RecipeLight recipeLight;
    private ChangeHandler changeHandler;


    public RecipeEditor(RecipeAPIClient recipeAPIClient) {
        this.recipeAPIClient = recipeAPIClient;
        name.setWidth("1000px");
        description.setWidth("1000px");
        comment.setWidth("1000px");
        //image.setWidth("1000px");
        //image.setHeight("2000px");
        // bind using naming convention
        binder.bindInstanceFields(this);

        // Configure and style components
        setSpacing(true);

        save.getElement().getThemeList().add("primary");
        delete.getElement().getThemeList().add("error");

        addKeyPressListener(Key.ENTER, e -> saveSimple());

        // wire action buttons to save, delete and reset
        save.addClickListener(e -> saveSimple());
        delete.addClickListener(e -> delete());
        cancel.addClickListener(e -> editRecipe(recipeLight));
        rotateClockwise.addClickListener(e -> rotate(90));
        rotateReverseClockwise.addClickListener(e -> rotate(-90));
        setVisible(false);

        //configure upload button
        Upload upload = setUpUpload();

        //add all elements to GUI
        add(name, description, comment, actions, upload, image);


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
            byte[] rotateImage = rotateImage(recipe.getRecipeBinaryEntity().getBinaryDescription(), angle);
            recipe.getRecipeBinaryEntity().setBinaryDescription(rotateImage);
            saveSimple();
        } catch (IOException e) {
            log.error("Error rotating image", e);
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

        if (recipe.getRecipeBinaryEntity() != null && recipe.getRecipeBinaryEntity().getBinaryDescription() != null) {

            try {
                byte[] imageScaled = geScaledImage(recipe.getRecipeBinaryEntity().getBinaryDescription(), 1200);
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

    private byte[] rotateImage(byte[] imageAsByteArray, double angle) throws IOException {

        BufferedImage img = null;
        try {
            img = ImageIO.read(new ByteArrayInputStream(imageAsByteArray));
        } catch (IOException e) {
            e.printStackTrace();
        }

        double sin = Math.abs(Math.sin(Math.toRadians(angle))),
                cos = Math.abs(Math.cos(Math.toRadians(angle)));

        int w = img.getWidth(null), h = img.getHeight(null);

        int neww = (int) Math.floor(w * cos + h * sin),
                newh = (int) Math.floor(h * cos + w * sin);

        BufferedImage bimg = new BufferedImage(neww, newh, img.getType());
        Graphics2D g = bimg.createGraphics();

        g.translate((neww - w) / 2, (newh - h) / 2);
        g.rotate(Math.toRadians(angle), w / 2, h / 2);
        g.drawRenderedImage(img, null);
        g.dispose();

        return toByteArray(bimg);

    }

    private static byte[] toByteArray(BufferedImage originalImage) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(originalImage, "jpg", baos);
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();
        return imageInByte;
    }

    //Utils for resizing image at scale
    public static byte[] geScaledImage(byte[] imageAsByteArray, int targetSize) throws IOException {

        ByteArrayInputStream bais = new ByteArrayInputStream(imageAsByteArray);

        BufferedImage originalImage = ImageIO.read(bais);

        originalImage = Scalr.resize(originalImage, targetSize);

        return toByteArray(originalImage);


    }


}