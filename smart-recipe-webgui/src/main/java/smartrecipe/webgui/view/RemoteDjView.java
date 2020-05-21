package smartrecipe.webgui.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.StreamResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import remotedj.model.DjTrackLightDto;
import remotedj.model.SpotifyUserDto;

import java.util.*;


@Slf4j
@Route("remotedj")
//@Push
public class RemoteDjView extends VerticalLayout {

    private Grid<SpotifyUserDto> grid = null;
    private Grid<DjTrackLightDto> gridTrackHistory = null;

    Anchor webLink = new Anchor("", "Authenticate in spotify after register user name");

    //text field
    private TextField clientNameTextField = new TextField("");

    //labels
    private Label message = new Label();
    private Label headerLabel = new Label();
    private Label readMeLabel = new Label();
    private Label readMeLabel2 = new Label();

    private Label currentDjTrackLabel = new Label();

    //button
    private final Button registerClientButton = new Button("Register user", VaadinIcon.MUSIC.create());
    private final Button getAllClientsAndTracksButton = new Button("Get all users and tracks history", VaadinIcon.USER.create());
    private final Button promoteDJButton = new Button("Promote user as DJ", VaadinIcon.STAR.create());
    private final Button removeClientButton = new Button("Remove selected client", VaadinIcon.DEL.create());
    private final Button removeAllClientsButton = new Button("Remove all clients", VaadinIcon.ERASER.create());
    private final Button createPlayListButton = new Button("Update playlist for user with DJ track", VaadinIcon.LIST.create());
    private final Button updatePlayListSingleTrackButton = new Button("Update playlist for user with selected track", VaadinIcon.LIST.create());

    @Value("${service.url}")
    private String serviceUrl;

    public RemoteDjView() {
        webLink.setTarget("_blank");

        //labels
        headerLabel.setText("REMOTE DJ");
        readMeLabel.setText("First restart spotify on your device and play one song to wake up it. " +
                "Then fill a name, register user and click on Authenticate link to finalize by spotify login");
        readMeLabel2.setText("Get all users/tracks will load current users connected/recent tracks played, promote user as DJ on selected" +
                " user will give him the control, create playlist for selected user will only add tracks broadcasted to him in the session");
        currentDjTrackLabel.getElement().getNode().markAsDirty();
        clientNameTextField.setPlaceholder("User name");

        StreamResource imageResource = new StreamResource("dj_logo_2.jpg", () -> {
            return RemoteDjView.class.getClassLoader().getResourceAsStream("DJ_Logo_web.jpg");
        });
        Image djImage = new Image(imageResource, "");
        djImage.setHeight("80px");
        djImage.setWidth("80px");

        //grid
        grid = new Grid<>(SpotifyUserDto.class);
        grid.setHeight("200px");
        grid.getColumnByKey("clientName").setHeader("User");

        gridTrackHistory = new Grid<>(DjTrackLightDto.class);
        //gridTrackHistory.setHeight("300px");
        gridTrackHistory.setColumns("trackName", "trackArtist", "clientName");
        gridTrackHistory.getColumnByKey("clientName").setWidth("200px").setFlexGrow(0);
        gridTrackHistory.getColumnByKey("trackName").setWidth("500px").setFlexGrow(0);
        gridTrackHistory.getColumnByKey("trackArtist").setWidth("500px").setFlexGrow(0);
        gridTrackHistory.getColumnByKey("clientName").setHeader("DJ");


        //buttons
        createPlayListButton.getElement().setAttribute("tooltip",
                "Create play list for user only with tracks broadcasted to him");


        //layout
        HorizontalLayout registerUserLayout = new HorizontalLayout();
        registerUserLayout.add(clientNameTextField,registerClientButton, webLink);
        HorizontalLayout userActionsLayout = new HorizontalLayout();
        userActionsLayout.add(getAllClientsAndTracksButton, promoteDJButton,
                updatePlayListSingleTrackButton, createPlayListButton);
        HorizontalLayout deleteUserActionsLayout = new HorizontalLayout();
        deleteUserActionsLayout.add(removeClientButton, removeAllClientsButton);
        HorizontalLayout gridLayout= new HorizontalLayout();
        gridLayout.add();


        deleteUserActionsLayout.add();
        add(djImage, readMeLabel, readMeLabel2, registerUserLayout,
                userActionsLayout, deleteUserActionsLayout, message, currentDjTrackLabel, gridTrackHistory, grid);

        //actions
        registerClientButton.addClickListener(e -> registerClient());
        getAllClientsAndTracksButton.addClickListener(e -> loadAllUsersAndTracksHistory());
        promoteDJButton.addClickListener(e -> promoteAsDjAction());
        removeClientButton.addClickListener(e -> removeClientAction());
        removeAllClientsButton.addClickListener(e -> removeAllClientsAndTracksAction());
        createPlayListButton.addClickListener(e -> createPlayListForClient());
        updatePlayListSingleTrackButton.addClickListener(e -> updatePlayList());
    }

    class DjTrackFeeder extends Thread {

        int count = 0;

        @Override
        public void run() {
            try {
                // Update the data for a while
                while (count < 100) {
                    Thread.sleep(1000);
                    UI.getCurrent().access((Command) () -> currentDjTrackLabel.setText("Count :" + count));
                    UI.getCurrent().push();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private void promoteAsDjAction() {
        Set<SpotifyUserDto> selectedUsers = grid.getSelectedItems();
        if (CollectionUtils.isEmpty(selectedUsers) || selectedUsers.size() > 1) {
            message.setText("You must select 1 user (only 1)");
            return;
        }
        String clientName = selectedUsers.stream().findFirst().get().getClientName();

        //call api
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getForEntity(serviceUrl +
                "/remotedj/setdj/" +
                clientName, Object.class);

        log.info("User promoted as DJ {0}", clientName);

        loadAllUsersAndTracksHistory();

        message.setText("User promoted as DJ " + clientName);
    }

    private void updatePlayList() {
        Set<SpotifyUserDto> selectedUsers = grid.getSelectedItems();
        if (CollectionUtils.isEmpty(selectedUsers) || selectedUsers.size() > 1) {
            message.setText("You must select 1 user (only 1)");
            return;
        }
        String clientName = selectedUsers.stream().findFirst().get().getClientName();

        //call api
        RestTemplate restTemplate = new RestTemplate();


        Set<DjTrackLightDto> selectedTracks = gridTrackHistory.getSelectedItems();
        if (CollectionUtils.isEmpty(selectedUsers) || selectedUsers.size() > 1) {
            message.setText("You must select 1 track (only 1)");
            return;
        }

        DjTrackLightDto track = selectedTracks.stream().findFirst().get();

        restTemplate.getForEntity(serviceUrl +
                "/remotedj/addtoplaylist/" +
                clientName + "/" + track.getTrackUri(), Object.class);

        log.info("Track {} added to playlist for user {}", track.getTrackName(), clientName);

        loadAllUsersAndTracksHistory();

        message.setText("Track added to playlist for user " + clientName);
    }


    private void createPlayListForClient() {
        Set<SpotifyUserDto> selectedUsers = grid.getSelectedItems();
        if (CollectionUtils.isEmpty(selectedUsers) || selectedUsers.size() > 1) {
            message.setText("You must select 1 user (only 1)");
            return;
        }
        String clientName = selectedUsers.stream().findFirst().get().getClientName();

        //call api
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getForEntity(serviceUrl +
                "/remotedj/createplaylist/" +
                clientName, Object.class);

        log.info("Playlist created for user (0)", clientName);

        message.setText("Playlist created for user " + clientName);
    }

    private void removeClientAction() {
        Set<SpotifyUserDto> selectedUsers = grid.getSelectedItems();
        if (CollectionUtils.isEmpty(selectedUsers) || selectedUsers.size() > 1) {
            message.setText("You must select 1 user (only 1)");
            return;
        }
        String clientName = selectedUsers.stream().findFirst().get().getClientName();

        //call api
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getForEntity(serviceUrl +
                "/remotedj/removeclient/" +
                clientName, Object.class);

        log.info("User removed {0}", clientName);

        loadAllUsersAndTracksHistory();

        message.setText("User removed " + clientName);
    }

    private void removeAllClientsAndTracksAction() {
        //call api
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getForEntity(serviceUrl +
                "/remotedj/removeallclient/", Object.class);

        log.info("All users removed");

        loadAllUsersAndTracksHistory();
        message.setText("All users removed");
    }


    private void registerClient() {

        if (StringUtils.isEmpty(clientNameTextField.getValue())) {
            message.setText("Fill user name");
            return;
        }

        //call api
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response =
                restTemplate.getForEntity(serviceUrl +
                                "/remotedj/registerclient/" +
                                clientNameTextField.getValue(),
                        String.class);

        log.info("Client registring status " + response.getBody());

        message.setText("Click on athenticate link above to login in spotify");
        webLink.setHref(response.getBody());

    }

    private void loadAllUsersAndTracksHistory() {

        // new DjTrackFeeder().run();

        //call api to load users
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<SpotifyUserDto[]> response =
                restTemplate.getForEntity(serviceUrl +
                                "/remotedj/getusersconnected",
                        SpotifyUserDto[].class);

        List<SpotifyUserDto> data = Arrays.asList(response.getBody());
        log.info("Number of total sensor data loaded: " + data.size());

        if (CollectionUtils.isEmpty(data)) {
            grid.setItems(Collections.emptyList());
            message.setText("No user connected");
            return;
        }

        //set users grid items
        grid.setItems(data);

        log.info("Number of users loaded: " + data.size());

        //call api to load tracks
        restTemplate = new RestTemplate();
        ResponseEntity<DjTrackLightDto[]> responseTracks =
                restTemplate.getForEntity(serviceUrl +
                                "/remotedj/getdjtracks",
                        DjTrackLightDto[].class);

        List<DjTrackLightDto> tracks = Arrays.asList(responseTracks.getBody());

        log.info("Number of tracks loaded: " + tracks.size());

        if (CollectionUtils.isEmpty(tracks)) {
            gridTrackHistory.setItems(Collections.emptyList());
            return;
        }

        tracks.sort(new Comparator<DjTrackLightDto>() {
            @Override
            public int compare(DjTrackLightDto o1, DjTrackLightDto o2) {
                return o2.getAddTimestamp().compareTo(o1.getAddTimestamp());
            }
        });

        //set tracks grid items
        gridTrackHistory.setItems(tracks);

        //get current track
        restTemplate = new RestTemplate();
        ResponseEntity<String> responseTrack =
                restTemplate.getForEntity(serviceUrl +
                                "/remotedj/gettrack",
                        String.class);
        if (responseTrack != null) {
            currentDjTrackLabel.setText(responseTrack.getBody());
        }

        message.setText("Users and tracks history loaded");

    }


}
