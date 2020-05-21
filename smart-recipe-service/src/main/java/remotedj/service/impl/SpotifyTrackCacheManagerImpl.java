package remotedj.service.impl;

import com.wrapper.spotify.model_objects.specification.Track;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.core.Ehcache;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;
import remotedj.model.DjTrackEntryDto;
import remotedj.model.DjTrackLightDto;
import remotedj.service.SpotifyTrackCacheManagerI;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@EnableCaching
public class SpotifyTrackCacheManagerImpl implements SpotifyTrackCacheManagerI {

    private CacheManager cacheManager;
    private Cache<DjTrackEntryDto, Long> tracksCache;

    public SpotifyTrackCacheManagerImpl() {
        cacheManager = CacheManagerBuilder
                .newCacheManagerBuilder().build();
        cacheManager.init();

        tracksCache = cacheManager
                .createCache("tracksCache", CacheConfigurationBuilder
                        .newCacheConfigurationBuilder(
                                DjTrackEntryDto.class, Long.class,
                                ResourcePoolsBuilder.heap(1000)).withExpiry(Expirations.timeToLiveExpiration(Duration.of(24,
                                TimeUnit.HOURS))));
    }

    @Override
    public void addToTracksCache(Track track, String clientName) {
        DjTrackEntryDto djTrackEntryDto = DjTrackEntryDto.builder().track(track).clientName(clientName).addTimestamp(System.currentTimeMillis()).build();
        cacheManager.getCache("tracksCache", DjTrackEntryDto.class, Long.class).put(djTrackEntryDto, System.currentTimeMillis());
    }
    @Override
    public Set<DjTrackLightDto> getCacheKeys(){
        Ehcache cache = (Ehcache) cacheManager.getCache("tracksCache", DjTrackEntryDto.class, Long.class);
        Iterator<Cache.Entry<DjTrackEntryDto, Long>> iterator = cache.iterator();
        Set<DjTrackLightDto> djTrackEntryDtoSet = new HashSet<>();
        while(iterator.hasNext()){
            DjTrackEntryDto djTrackEntryDto = iterator.next().getKey();
            djTrackEntryDtoSet.add(DjTrackLightDto.builder().
                    clientName(djTrackEntryDto.getClientName()).
                    trackName(djTrackEntryDto.getTrack().getName()).
                    trackArtist((djTrackEntryDto.getTrack().getArtists()[0]).getName()).
                    addTimestamp(djTrackEntryDto.getAddTimestamp()).
                    trackUri(djTrackEntryDto.getTrack().getUri()).build());
        }
        return djTrackEntryDtoSet;
    }
}
