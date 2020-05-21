package remotedj.controller;


import com.wrapper.spotify.model_objects.specification.Track;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import remotedj.service.SpotifyTrackCacheManagerI;
import remotedj.service.SpotifyUserService;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Slf4j
@RestController
public class RemoteDjController {

    @Autowired
    SpotifyUserService spotifyUserService;

    @Autowired
    SpotifyTrackCacheManagerI spotifyTrackCacheManager;

    @GetMapping("/remotedj/test")
    @ApiOperation("Spotify test")
    String testdj() throws Exception {
        return "SPOTIFY OK";
    }


    @GetMapping("/remotedj/registerclient/{clientName}")
    @ApiOperation("Register client by name")
    String registerClient(@PathVariable("clientName") String clientName) throws Exception {
        return spotifyUserService.registerClient(clientName);
    }

    @GetMapping("/remotedj/setdj/{clientName}")
    @ApiOperation("Promote user as dj")
    void promoteUserAsDj(@PathVariable("clientName") String clientName) throws Exception {
        spotifyUserService.setUserAsDj(clientName);
    }

    @GetMapping("/remotedj/removeclient/{clientName}")
    @ApiOperation("Remove client")
    void removeClient(@PathVariable("clientName") String clientName) throws Exception {
        spotifyUserService.removeClient(clientName);
    }

    @GetMapping("/remotedj/createplaylist/{clientName}")
    @ApiOperation("Create playlist for user")
    void createPlayListForClient(@PathVariable("clientName") String clientName) throws Exception {
        spotifyUserService.addDjTracksToUserPlayList(clientName);
    }

    @GetMapping("/remotedj/addtoplaylist/{clientName}/{trackUri}")
    @ApiOperation("Add a song to playlist")
    void addAtrackToPlaylist(@PathVariable("clientName") String clientName, @PathVariable("trackUri")  String trackUri) throws Exception {
        spotifyUserService.addTrackToUserPlayList(clientName, Collections.singletonList(trackUri));
    }


    @GetMapping("/remotedj/removeallclient/")
    @ApiOperation("Remove all clients")
    void removeAllClients() throws Exception {
        spotifyUserService.removeAllClient();
    }


    @GetMapping("/remotedj/broadcastdjtrack")
    @ApiOperation("Set DJ track to all")
    void broadcastDjTrackToAll() throws Exception {
        spotifyUserService.broadcastDjCurrentTrackToAll(spotifyUserService.getDjCurrentTrack());
    }

    @GetMapping("/remotedj/getusersconnected")
    @ApiOperation("Get users connected")
    Collection getUsersConnected() throws Exception {
        return spotifyUserService.getSpotifyUsersConnected();
    }

    @GetMapping("/remotedj/getdjtracks")
    @ApiOperation("Get DJ track history")
    Set getDjTracksHistory() throws Exception {
        return spotifyTrackCacheManager.getCacheKeys();
    }

    @GetMapping("/remotedj/callback")
    @ApiOperation("Spotify callback")
    String spotifyCallback(@RequestParam(name = "code") String code) throws Exception {
        return spotifyUserService.spotifyAuthenticationCallback(code);
    }

    @GetMapping("/remotedj/gettrack")
    @ApiOperation("Spotify get current track of dj")
    String getDjCurrentTrack() throws Exception {
        Track currentDjTrack = spotifyUserService.getDjCurrentTrack();
        return "Last recent DJ track is " + currentDjTrack.getArtists()[0].getName() + " - " + currentDjTrack.getName();
    }

}
