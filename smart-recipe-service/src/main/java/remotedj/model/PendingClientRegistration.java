package remotedj.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PendingClientRegistration {

    private String clientNAme;
    private long timeRegistrationStart;

}
