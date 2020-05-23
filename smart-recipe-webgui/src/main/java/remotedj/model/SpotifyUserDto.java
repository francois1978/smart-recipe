package remotedj.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotifyUserDto implements Comparable<SpotifyUserDto> {

    private String clientName;
    private boolean dj;
    private String userLog;

    @Override
    public int compareTo(SpotifyUserDto o) {
        return o.isDj() ? 1 : -1;
    }
}
