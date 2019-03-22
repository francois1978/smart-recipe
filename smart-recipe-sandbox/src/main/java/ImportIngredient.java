import dataloader.clientapi.AbstractAPIClient;
import dataloader.clientapi.IngredientAPIClient;
import dataloader.clientapi.PlateTypeAPIClient;
import dataloader.entity.Entity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.List;

@Slf4j
public class ImportIngredient {


    public static void main2(String args[]) {

        String pathtoRecipe = "C:\\dev\\temp\\ingredients";
        String pathToPlateType = "C:\\dev\\temp\\type_plat";

        AbstractAPIClient ingredientAPIClient = new IngredientAPIClient();
        AbstractAPIClient plateTypeAPIClient = new PlateTypeAPIClient();


        parseFile(pathtoRecipe, ingredientAPIClient);
        parseFile(pathToPlateType, plateTypeAPIClient);


    }

    private static void parseFile(String pathtoRecipe, AbstractAPIClient apiClient) {
        File dir = new File(pathtoRecipe);

        //Once you have the appropriate path, you can iterate through its contents:
        //List directory
        // si le repertoire courant est bien un repertoire
        File[] files = dir.listFiles();

        for (File file : files) {

            if (file.isDirectory()) {
                continue;
            }
            log.info("processing file:" + file.getName());
            try {
                InputStream flux = new FileInputStream(file);
                InputStreamReader lecture = new InputStreamReader(flux);
                BufferedReader buff = new BufferedReader(lecture);
                String ligne;
                while ((ligne = buff.readLine()) != null) {
                    if (StringUtils.isAlphanumeric(ligne)) {
                        String name = StringUtils.remove(ligne, " ");
                        log.info("Processing word: " + name);
                        //check if element exists in DB
                        List existingElements = apiClient.findByName(name);
                        //create element if not exist
                        if (existingElements == null || existingElements.size() == 0) {
                            Entity entity = apiClient.buildNewEntity(name);
                            apiClient.create(entity);
                        }
                    }
                }
                buff.close();
            } catch (Exception e) {
                log.error("Error while parsing element file", e);
            }
        }
    }
}
