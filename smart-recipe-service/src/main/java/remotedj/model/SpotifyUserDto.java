package remotedj.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
public class SpotifyUserDto {

    private String code;
    private String token;
    private String refreshToken;
    private long tokenEndTime;
    private int tokenRefreshCountTime;
    private String clientName;
    private String userId;
    private boolean dj;
    private Set<DjTrackEntryDto> djTrackEntryDtos;
    private String playlistId;
    private String userLog;

    public void addToDjTracks(DjTrackEntryDto track){
        if(djTrackEntryDtos==null){
            djTrackEntryDtos = new HashSet<>();
        }
        djTrackEntryDtos.add(track);
    }

    public void incrementTokenRefreshCountTime(){
        tokenRefreshCountTime = tokenRefreshCountTime+1;
    }

    public void clearUserLog(){
        userLog = "";
    }

}
