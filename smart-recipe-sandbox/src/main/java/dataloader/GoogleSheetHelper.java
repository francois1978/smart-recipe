package dataloader;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GoogleSheetHelper {
    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    //private static final String CREDENTIALS_FILE_PATH = "C:\\dev\\program\\smartrecipe\\config\\code_secret_client_323516178132-ns868gpphm103e0hdpndv8pv0sb9t5u5.apps.googleusercontent.com.json";
    private static final String CREDENTIALS_FILE_PATH = "C:\\dev\\program\\smartrecipe\\config\\credentials.json";
    private static final String CREDENTIALS_FILE_PATH2 = "C:\\dev\\program\\smartrecipe\\config\\smartrecipe-7cfaff0cdca4.json";

    private static Credential getCredentialsServerToServer() throws IOException {
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(CREDENTIALS_FILE_PATH2))
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
        return credential;
    }

    /**
     * Prints the names and majors of students in a sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
     */
    public static void main1(String... args) throws IOException, GeneralSecurityException {

        List<List<Object>> valuesToWrite = Arrays.asList(
                Arrays.asList(
                        "courgette"
                ),
                Arrays.asList(
                        "poivre"
                )
                // Additional rows ...
        );
        GoogleSheetHelper sheetsQuickstart = new GoogleSheetHelper();
        sheetsQuickstart.runUpdate(valuesToWrite);

    }

    public void runUpdate(List<List<Object>> valuesToWrite) throws GeneralSecurityException, IOException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = "1KX8VkbSj1i03K7vdmM33xAn3YKF7eEfhJxfrqemBgD4";
        final String range = "data!A:A";
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentialsServerToServer())
                .setApplicationName(APPLICATION_NAME)
                .build();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            System.out.println("Name, Major");
            for (List row : values) {
                // Print columns A and E, which correspond to indices 0 and 4.
                System.out.println(row.get(0));
            }
        }


        ValueRange body = new ValueRange()
                .setValues(valuesToWrite);
        UpdateValuesResponse result =
                service.spreadsheets().values().update(spreadsheetId, range, body)
                        .setValueInputOption("RAW")
                        .execute();
    }
}