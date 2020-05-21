package remotedj.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotifyUserDto {

    private String clientName;
    private boolean dj;

}
