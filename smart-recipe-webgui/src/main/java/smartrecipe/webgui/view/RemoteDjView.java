package smartrecipe.webgui.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import remotedj.model.DjTrackLightDto;
import remotedj.model.SpotifyUserDto;

import java.util.*;


@Slf4j
@Route("remotedj")
@Push
public class RemoteDjView extends VerticalLayout {

    private Grid<SpotifyUserDto> grid = null;
    private Grid<DjTrackLightDto> gridTrackHistory = null;

    Anchor webLink = new Anchor("", "POP UP blocker: Click here to login in spotify after registering user name");

    //text field
    private TextField clientNameTextField = new TextField("");

    //labels
    private Label message = new Label();
    //private Label currentDJTrackLabel = new Label();
    private Label headerLabel = new Label();
    private Label readMeLabel = new Label();
    private Label readMeLabel2 = new Label();

    private Label currentDjTrackLabel = new Label();

    //button
    private final Button registerClientButton = new Button("Register user", VaadinIcon.USER_CHECK.create());
    private final Button getAllClientsAndTracksButton = new Button("Refresh users and tracks history", VaadinIcon.USERS.create());

    @Value("${service.url}")
    private String serviceUrl;

    public RemoteDjView() {
        webLink.setTarget("_blank");

        //labels
        initLabelsTextFields();

        //image
        Image djImage = getDjImage();
        djImage.setHeight("80px");
        djImage.setWidth("80px");

        //grid
        initGrids();

        //buttons
        initButtons();

        //layout
        HorizontalLayout registerUserLayout = new HorizontalLayout();
        registerUserLayout.add(clientNameTextField, registerClientButton, webLink);
        HorizontalLayout userActionsLayout = new HorizontalLayout();

        //add components
        add(djImage, readMeLabel, readMeLabel2, registerUserLayout,
                getAllClientsAndTracksButton, message, currentDjTrackLabel, grid, gridTrackHistory);

        //actions
        initActions();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {

        // Start the data feed thread
        new DjTrackFeeder(attachEvent.getUI(), this).start();
    }


    private Image getDjImage() {
        StreamResource imageResource = new StreamResource("dj_logo_2.jpg", () -> {
            return RemoteDjView.class.getClassLoader().getResourceAsStream("DJ_Logo_web.jpg");
        });
        return new Image(imageResource, "");
    }

    private void initActions() {
        registerClientButton.addClickListener(e -> registerClient());
        getAllClientsAndTracksButton.addClickListener(e -> loadAllUsersAndTracksHistory());
    }

    private void initButtons() {
        getAllClientsAndTracksButton.setWidth("300px");
        registerClientButton.setWidth("300px");
    }

    private void initGrids() {
        grid = new Grid<>(SpotifyUserDto.class);
        grid.setHeight("200px");
        grid.removeColumnByKey("clientName");
        grid.removeColumnByKey("dj");
        grid.removeColumnByKey("userLog");

        grid.addComponentColumn(
                user -> {
                    HorizontalLayout layout = new HorizontalLayout();
                    Label userNameLabel = new Label();

                    String imageSize = "30px";
                    if (user.isDj()) {
                        Image djImage = getDjImage();
                        djImage.setWidth(imageSize);
                        djImage.setHeight(imageSize);
                        userNameLabel.setText("DJ " + user.getClientName());
                        layout.add(djImage, userNameLabel);
                    } else {
                        Icon iconListener = VaadinIcon.HEADPHONES.create();
                        iconListener.setSize(imageSize);
                        userNameLabel.setText(user.getClientName());
                        layout.add(iconListener, userNameLabel);
                    }
                    return layout;
                }
        ).setHeader("User (DJ/Fan club)");


        grid.addComponentColumn(
                user -> {
                    Button button = new Button("Promote as DJ");
                    button.setIcon(VaadinIcon.STAR.create());
                    button.addClickListener(e -> promoteAsDjAction(user.getClientName()));
                    return button;
                }
        ).setHeader("Promote as DJ");

        grid.addComponentColumn(
                user -> {
                    Button button = new Button("Add current track");
                    button.setIcon(VaadinIcon.LIST_SELECT.create());
                    button.addClickListener(e -> updatePlayListWithCurrenTrack(user.getClientName()));
                    return button;
                }
        ).setHeader("Add current track to playlist");


        grid.addComponentColumn(
                user -> {
                    Button button = new Button("Add all tracks");
                    button.setIcon(VaadinIcon.LIST_UL.create());
                    button.addClickListener(e -> updatePlayListWithDjTracks(user.getClientName()));
                    return button;
                }
        ).setHeader("Add all track listen to playlist");

        grid.addComponentColumn(
                user -> {
                    Button button = new Button("Remove user");
                    button.setIcon(VaadinIcon.ERASER.create());
                    button.addClickListener(e -> removeClientAction(user.getClientName()));
                    return button;
                }
        ).setHeader("Remove user");

        grid.addComponentColumn(
                user -> {
                    Label userNameLabel = new Label(user.getUserLog());
                    userNameLabel.getStyle().set("white-space", "normal");
                    return userNameLabel;
                }
        ).setHeader("User info").setWidth("200px");


        gridTrackHistory = new Grid<>(DjTrackLightDto.class);
        //gridTrackHistory.setHeight("300px");
        gridTrackHistory.setColumns("trackName", "trackArtist", "clientName");
        gridTrackHistory.getColumnByKey("clientName").setWidth("200px").setFlexGrow(0);
        gridTrackHistory.getColumnByKey("trackName").setWidth("500px").setFlexGrow(0);
        gridTrackHistory.getColumnByKey("trackArtist").setWidth("500px").setFlexGrow(0);
        gridTrackHistory.getColumnByKey("clientName").setHeader("DJ");
    }

    private void initLabelsTextFields() {
        message.getStyle().set("color", "blue");
        headerLabel.setText("REMOTE DJ");
        readMeLabel.setText("First restart spotify on your device and play one song to wake up it. " +
                "Then fill a name, register user and a new page will be loaded for Spotify authentication (in case of pop up blocker click on Authenticate link to login in Spotify)");
        readMeLabel2.setText("Then refresh users list !");
        currentDjTrackLabel.getElement().getNode().markAsDirty();
        clientNameTextField.setPlaceholder("User name");
        clientNameTextField.setWidth("300px");
    }

    private void promoteAsDjAction(String userName) {
        Set<SpotifyUserDto> selectedUsers = null;
        if (userName == null) {
            selectedUsers = getUserSelected();
            if (selectedUsers == null) return;
        }
        String clientName = userName != null ? userName : selectedUsers.stream().findFirst().get().getClientName();

        //call api
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getForEntity(serviceUrl +
                "/remotedj/setdj/" +
                clientName, Object.class);

        log.info("User promoted as DJ {}", clientName);

        loadAllUsersAndTracksHistory();

        message.setText("A new DJ has been promoted: DJ " + clientName);
    }

    private Set<SpotifyUserDto> getUserSelected() {
        Set<SpotifyUserDto> selectedUsers = grid.getSelectedItems();
        if (CollectionUtils.isEmpty(selectedUsers) || selectedUsers.size() > 1) {
            message.setText("You must select 1 user (only 1)");
            return null;
        }
        return selectedUsers;
    }

    private void updatePlayListWithCurrenTrack(String userName) {
        Set<SpotifyUserDto> selectedUsers = null;
        if (userName == null) {
            selectedUsers = getUserSelected();
            if (selectedUsers == null) return;
        }
        String clientName = userName != null ? userName : selectedUsers.stream().findFirst().get().getClientName();

        //call api
        RestTemplate restTemplate = new RestTemplate();
/*
        Set<DjTrackLightDto> selectedTracks = gridTrackHistory.getSelectedItems();
        if (CollectionUtils.isEmpty(selectedUsers) || selectedUsers.size() > 1) {
            message.setText("You must select 1 track (only 1)");
            return;
        }

        DjTrackLightDto track = selectedTracks.stream().findFirst().get();*/

        ResponseEntity<String> response = restTemplate.getForEntity(serviceUrl +
                "/remotedj/addcurrenttrack/" +
                clientName, String.class);

        log.info(response.getBody());

        loadAllUsersAndTracksHistory();

        message.setText(response.getBody());
    }


    private void updatePlayListWithDjTracks(String userName) {
        Set<SpotifyUserDto> selectedUsers = null;
        if (userName == null) {
            selectedUsers = getUserSelected();
            if (selectedUsers == null) return;
        }
        String clientName = userName != null ? userName : selectedUsers.stream().findFirst().get().getClientName();

        //call api
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.getForEntity(serviceUrl +
                "/remotedj/addalltoplaylist/" +
                clientName, String.class);

        log.info("Playlist updated for user ()", clientName);

        message.setText(response.getBody());
    }

    private void removeClientAction(String userName) {
        Set<SpotifyUserDto> selectedUsers = null;
        if (userName == null) {
            selectedUsers = getUserSelected();
            if (selectedUsers == null) return;
        }
        String clientName = userName != null ? userName : selectedUsers.stream().findFirst().get().getClientName();

        //call api
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getForEntity(serviceUrl +
                "/remotedj/removeclient/" +
                clientName, Object.class);

        log.info("User removed {}", clientName);

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
        ResponseEntity<String> response = null;
        try {
            RestTemplate restTemplate = new RestTemplate();
            response =
                    restTemplate.getForEntity(serviceUrl +
                                    "/remotedj/registerclient/" +
                                    clientNameTextField.getValue(),
                            String.class);
        } catch (RestClientException e) {
            message.setText(e.getMessage());
            return;
        }

        log.info("Client registring status " + response.getBody());

        message.setText("Click on athenticate link above to login in spotify");
        webLink.setHref(response.getBody());

        getUI().get().getPage().executeJavaScript(
                "window.open(\"" + response.getBody() + "\",\"_blank\")");


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
        log.info("Number of users loaded: " + data.size());

        if (CollectionUtils.isEmpty(data)) {
            grid.setItems(Collections.emptyList());
            message.setText("No user connected");
            return;
        }

        Collections.sort(data);

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
        ResponseEntity<String> responseTrack = getCurrentDjTrack();
        if (responseTrack != null) {
            currentDjTrackLabel.setText(responseTrack.getBody() + " (refresh to update)");
        }

        message.setText("Users and tracks history loaded");

    }

    private ResponseEntity<String> getCurrentDjTrack() {
        RestTemplate restTemplate2 = new RestTemplate();
        return restTemplate2.getForEntity(serviceUrl +
                        "/remotedj/gettrack",
                String.class);
    }

    class DjTrackFeeder extends Thread {

        private UI ui;
        private RemoteDjView view;

        public DjTrackFeeder(UI ui, RemoteDjView remoteDjView) {
            this.ui = ui;
            this.view = remoteDjView;
        }

        @Override
        public void run() {
            try {
                // Update the data for a while
                while (true) {
                    Thread.sleep(2000);

                    ResponseEntity<String> responseTrack = getCurrentDjTrack();
                    if (responseTrack != null) {
                        ui.access(() -> view.getCurrentDjTrackLabel().setText(responseTrack.getBody()));
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public Label getCurrentDjTrackLabel() {
        return currentDjTrackLabel;
    }
}
