package remotedj.service;

import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Track;
import org.apache.hc.core5.http.ParseException;
import remotedj.model.SpotifyUserDto;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface SpotifyUserService {
    Collection<SpotifyUserDto> getSpotifyUsersConnected();

    String registerClient(String clientName) throws Exception;

    //@Scheduled(fixedRate=3000)
    void refresjDjCurrentTrack() throws ParseException, SpotifyWebApiException, IOException;

    void broadcastDjCurrentTrackToAll(Track djTrack) throws ParseException, SpotifyWebApiException, IOException;

    String addDjTracksToUserPlayList(String clientName) throws ParseException, SpotifyWebApiException, IOException;

    String addTrackToUserPlayList(String clientName, List<String> trackUris) throws ParseException, IOException, SpotifyWebApiException;

    Track getDjCurrentTrack() throws ParseException, SpotifyWebApiException, IOException;

    String spotifyAuthenticationCallback(String code) throws Exception;

    void setUserAsDj(String clientNamePromotedToDj);

    void removeClient(String clientName);

    void removeAllClient();
}
