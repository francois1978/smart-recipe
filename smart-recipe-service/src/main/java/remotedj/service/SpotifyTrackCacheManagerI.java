package remotedj.service;

import com.wrapper.spotify.model_objects.specification.Track;
import remotedj.model.DjTrackLightDto;

import java.util.Set;

public interface SpotifyTrackCacheManagerI {
    void addToTracksCache(Track track, String clientName);

    Set<DjTrackLightDto> getCacheKeys();
}
