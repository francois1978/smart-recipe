package remotedj.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DjTrackLightDto {

    private String trackName;
    private String trackArtist;
    private String trackUri;
    private String clientName;
    private Long addTimestamp;
}
