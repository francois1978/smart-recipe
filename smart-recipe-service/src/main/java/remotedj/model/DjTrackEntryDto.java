package remotedj.model;

import com.wrapper.spotify.model_objects.specification.Track;
import lombok.*;

import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DjTrackEntryDto {

    private Track track;
    private String clientName;
    private Long addTimestamp;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DjTrackEntryDto that = (DjTrackEntryDto) o;
        return track.getUri().equals(that.track.getUri());
    }

    @Override
    public int hashCode() {
        return Objects.hash(track.getUri());
    }
}
