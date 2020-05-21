package remotedj.service.impl;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.specification.User;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import com.wrapper.spotify.requests.data.player.AddItemToUsersPlaybackQueueRequest;
import com.wrapper.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;
import com.wrapper.spotify.requests.data.player.PauseUsersPlaybackRequest;
import com.wrapper.spotify.requests.data.player.SkipUsersPlaybackToNextTrackRequest;
import com.wrapper.spotify.requests.data.playlists.AddItemsToPlaylistRequest;
import com.wrapper.spotify.requests.data.playlists.CreatePlaylistRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistRequest;
import com.wrapper.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import remotedj.model.DjTrackEntryDto;
import remotedj.model.PendingClientRegistration;
import remotedj.model.SpotifyUserDto;
import remotedj.service.SpotifyTrackCacheManagerI;
import remotedj.service.SpotifyUserService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SpotifyUserServiceImpl implements SpotifyUserService {

    private ConcurrentHashMap<String, SpotifyUserDto> userDtoByClientName = new ConcurrentHashMap<>();

    private String currentDjTrackUri = null;

    private PendingClientRegistration pendingClientRegistration;

    @Autowired
    private SpotifyTrackCacheManagerI spotifyTrackCacheManager;


    @Override
    public Collection<SpotifyUserDto> getSpotifyUsersConnected() {
        return userDtoByClientName.values();
    }

    @Autowired
    private RemoteDjConfigurationImpl remoteDjConfiguration;


    @Override
    @Scheduled(fixedRate = 2000)
    public void refresjDjCurrentTrack() throws ParseException, SpotifyWebApiException, IOException {
        Track newCurrentDjTrack = getDjCurrentTrack();

        if (newCurrentDjTrack != null
                && (currentDjTrackUri == null || !newCurrentDjTrack.getUri().equals(currentDjTrackUri))) {
            log.info("DJ current track changed: {}", newCurrentDjTrack.getName());
            currentDjTrackUri = newCurrentDjTrack.getUri();
            broadcastDjCurrentTrackToAll(newCurrentDjTrack);
            Optional<SpotifyUserDto> djSpotifyUserDtoOptionnal = userDtoByClientName.values().stream().filter(e -> e.isDj()).findFirst();
            spotifyTrackCacheManager.addToTracksCache(newCurrentDjTrack, djSpotifyUserDtoOptionnal.get().getClientName());
            //log.info("Current DJ track " + currentDjTrackUri);
        }
    }


    @Override
    public void broadcastDjCurrentTrackToAll(Track djTrack)
            throws ParseException, IOException {

        log.info("Broadcast track {} to all clients", djTrack.getName());

        if (djTrack == null) {
            return;
        }

        List<SpotifyUserDto> clientList = userDtoByClientName.values().stream().filter(e -> !e.isDj()).collect(Collectors.toList());

        for (SpotifyUserDto user : clientList) {

            log.info("Pushing track to user {}", user.getClientName());
            SpotifyApi spotifyApi = new SpotifyApi.Builder()
                    .setAccessToken(user.getToken())
                    .build();
            try {
                //add track to client queue
                AddItemToUsersPlaybackQueueRequest addItemToUsersPlaybackQueueRequest = spotifyApi
                        .addItemToUsersPlaybackQueue(djTrack.getUri())
                        .build();
                final String string = addItemToUsersPlaybackQueueRequest.execute();

                //go to next track in client queue
                SkipUsersPlaybackToNextTrackRequest skipUsersPlaybackToNextTrackRequest = spotifyApi
                        .skipUsersPlaybackToNextTrack()
                        .build();
                skipUsersPlaybackToNextTrackRequest.execute();
                user.addToDjTracks(DjTrackEntryDto.builder().track(djTrack).build());

                log.info("Track pushed to user {}", user.getClientName());

            } catch (SpotifyWebApiException e) {
                log.error("Error while pushing track to client {}", user.getClientName(), e);
            }
        }
    }

    @Override
    public void addDjTracksToUserPlayList(String clientName) throws ParseException, IOException, SpotifyWebApiException {
        SpotifyUserDto spotifyUserDto = userDtoByClientName.get(clientName);

        List<String> trackUris = spotifyUserDto.getDjTrackEntryDtos().stream().
                map(e -> e.getTrack().getUri()).collect(Collectors.toList());
        addTrackToUserPlayList(spotifyUserDto.getClientName(), trackUris);
    }


    @Override
    public void addTrackToUserPlayList(String clientName, List<String> trackUris) throws ParseException, IOException, SpotifyWebApiException {

        log.info("Create or update spotify playlist for user {}", clientName);

        if (CollectionUtils.isEmpty(trackUris)) return;

        SpotifyUserDto spotifyUserDto = userDtoByClientName.get(clientName);

        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setAccessToken(spotifyUserDto.getToken())
                .build();

        //create or load playlist
        Playlist playlist = null;

        //load existing day playlist
        if (spotifyUserDto.getPlaylistId() != null) {
            GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(spotifyUserDto.getPlaylistId()).build();
            try {
                playlist = getPlaylistRequest.execute();
            } catch (SpotifyWebApiException e) {
                log.error("Error while loading playlist {} for client {}", spotifyUserDto.getPlaylistId(), clientName, e);
            }
        }
        if (playlist == null) {
            try {

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy");
                String playListName = "RemoteDJ_" + dateFormat.format(new Date());
                CreatePlaylistRequest createPlaylistRequest = spotifyApi.
                        createPlaylist(spotifyUserDto.getUserId(), playListName)
                        //.collaborative(true)
                        .public_(true)
                        .description("Playlist created by Remote DJ, enjoy your friends gros son (ou pas)")
                        .build();
                playlist = createPlaylistRequest.execute();
                spotifyUserDto.setPlaylistId(playlist.getId());

            } catch (SpotifyWebApiException e) {
                log.error("Error while creating playlist for client {}", clientName, e);
                throw e;
            }
        }

        //add items to playlist
        String[] trackUrisAsArray = new String[trackUris.size()];
        trackUris.toArray(trackUrisAsArray);
        AddItemsToPlaylistRequest addItemsToPlaylistRequest =
                spotifyApi.addItemsToPlaylist(playlist.getId(), trackUrisAsArray).build();

        try {
            addItemsToPlaylistRequest.execute();
        } catch (SpotifyWebApiException e) {
            log.error("Error while adding tracks to playlist for client {}", clientName, e);
            throw e;
        }

        log.info("Tracks added to user playlist {}, tracks count {}", playlist.getName(), trackUris.size());

    }


    @Override
    public Track getDjCurrentTrack() throws ParseException, SpotifyWebApiException, IOException {

        Optional<SpotifyUserDto> djSpotifyUserDtoOptionnal = userDtoByClientName.values().stream().filter(e -> e.isDj()).findFirst();

        //no dj found
        if (!djSpotifyUserDtoOptionnal.isPresent()) return null;

        //get current track of dj
        SpotifyUserDto djSpotifyUserDto = djSpotifyUserDtoOptionnal.get();
        try {
            SpotifyApi spotifyApi2 = new SpotifyApi.Builder()
                    .setAccessToken(djSpotifyUserDto.getToken())
                    .build();
            PauseUsersPlaybackRequest pauseUsersPlaybackRequest = spotifyApi2.pauseUsersPlayback()
                    .build();

            GetUsersCurrentlyPlayingTrackRequest getUsersCurrentlyPlayingTrackRequest = spotifyApi2
                    .getUsersCurrentlyPlayingTrack().build();
            final CurrentlyPlaying currentlyPlaying = getUsersCurrentlyPlayingTrackRequest.execute();
            if (currentlyPlaying == null) {
                return null;
            }
            Track track = ((Track) currentlyPlaying.getItem());
            djSpotifyUserDto.addToDjTracks(DjTrackEntryDto.builder().track(track).build());
            log.debug("DJ current track name: " + track.getName());
            //currentDjTrackUri = ((Track) currentlyPlaying.getItem()).getUri();
            return track;
        } catch (Exception e) {
            log.error("Error while getting current DJ track", e);
            throw e;
        }
    }

    @Override
    public synchronized String registerClient(String clientName) throws Exception {
        //check if another client is not in registration since less than 1 min
        if (pendingClientRegistration != null) {
            if (System.currentTimeMillis() - pendingClientRegistration.getTimeRegistrationStart() < 60000) {
                throw new Exception("Another client authentication already in pogress");
            } else {
                pendingClientRegistration = null;
            }
        }

        log.info("Register client {}", clientName);
        pendingClientRegistration = PendingClientRegistration.builder().
                clientNAme(clientName).
                timeRegistrationStart(System.currentTimeMillis()).
                build();

        SpotifyApi spotifyApi = buildSpotifyApi();

        AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
//          .state("x4xkmn9pu3j6ukrs8n")
                .scope("user-modify-playback-state,user-read-currently-playing,user-read-email,playlist-modify-public")
//          .show_dialog(true)
                .build();

        final URI uri = authorizationCodeUriRequest.execute();

        log.info("Client {} registring process can carry on with link {}", clientName, uri.toString());

        return uri.toString();
    }


    @Override
    public synchronized String spotifyAuthenticationCallback(String code) throws Exception {

        try {
            log.info("Handling spotify call back for authentication, client name {}, code {}",
                    pendingClientRegistration.getClientNAme(), code);
            SpotifyApi spotifyApi = buildSpotifyApi();

            AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(code)
                    .build();
            AuthorizationCodeCredentials authorizationCodeCredentials = null;

            try {
                authorizationCodeCredentials = authorizationCodeRequest.execute();
                // Set access and refresh token for further "spotifyApi" object usage
                log.info("Token for client name {} is {}, expiry in {}",
                        pendingClientRegistration.getClientNAme(),
                        authorizationCodeCredentials.getAccessToken(),
                        authorizationCodeCredentials.getExpiresIn());

                //get user infos
                spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
                GetCurrentUsersProfileRequest getCurrentUsersProfileRequest = spotifyApi.getCurrentUsersProfile()
                        .build();
                final User user = getCurrentUsersProfileRequest.execute();

                SpotifyUserDto spotifyUserDto =
                        SpotifyUserDto.builder().dj(userDtoByClientName.isEmpty() ? true : false).
                                clientName(pendingClientRegistration.getClientNAme()).
                                code(code).
                                userId(user.getId()).
                                token(authorizationCodeCredentials.getAccessToken()).
                                tokenEndTime(System.currentTimeMillis() + authorizationCodeCredentials.getExpiresIn() * 1000).
                                build();
                userDtoByClientName.put(pendingClientRegistration.getClientNAme(), spotifyUserDto);

            } catch (Exception e) {
                log.error("Error while managing spotify callback", e);
                throw e;
            }
        } finally {
            //reset client name for next aysnhcronous code recpetion
            pendingClientRegistration = null;
        }
        return "Token created for spotify client account";
    }

    private SpotifyApi buildSpotifyApi() throws URISyntaxException {
        return new SpotifyApi.Builder()
                .setClientId(remoteDjConfiguration.getClientId())
                .setClientSecret(remoteDjConfiguration.getClientSecret())
                .setRedirectUri(new URI(remoteDjConfiguration.getSpotifyCallBack()))
                .build();
    }

    @Override
    public void setUserAsDj(String clientNamePromotedToDj) {
        userDtoByClientName.values().stream().forEach(e -> {

            if (e.getClientName().equalsIgnoreCase(clientNamePromotedToDj)) {
                e.setDj(true);
            } else {
                e.setDj(false);
            }
        });

    }

    @Override
    public synchronized void removeClient(String clientName) {
        log.info("Removing client {}", clientName);
        userDtoByClientName.remove(clientName);
    }

    @Override
    public synchronized void removeAllClient() {
        log.info("Removing all clients");
        userDtoByClientName.clear();
    }


    @Scheduled(fixedDelay = 30000)
    private void refreshToken() throws URISyntaxException, IOException, ParseException {

        Collection<SpotifyUserDto> userListCopy = new ArrayList<>(userDtoByClientName.values());

        for (SpotifyUserDto userDto : userListCopy) {

            //refresh if expiry in less than n minutes
            if (userDto.getTokenEndTime() - System.currentTimeMillis() < 5 * 60 * 1000) {

                //limit refresh count
                if (userDto.getTokenRefreshCountTime() >= 1) {
                    //remove user from list
                    log.info("Token expiry soon and max refresh count reached, Removing user {}", userDto.getClientName());
                    userDtoByClientName.remove(userDto.getClientName());
                    continue;
                }
                SpotifyApi spotifyApi = buildSpotifyApi();
                spotifyApi.setRefreshToken(userDto.getToken());
                AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh()
                        .build();

                AuthorizationCodeCredentials authorizationCodeCredentials;
                try {
                    authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();
                    userDto.setToken(authorizationCodeCredentials.getAccessToken());
                    userDto.setTokenEndTime(System.currentTimeMillis() + authorizationCodeCredentials.getExpiresIn() * 1000);
                    userDto.setTokenRefreshCountTime(userDto.getTokenRefreshCountTime() + 1);
                } catch (SpotifyWebApiException e) {
                    log.error("Unable to refresh token for user (skipping) {}", userDto.getClientName(), e);
                }
            }
        }
    }
}