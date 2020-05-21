package remotedj.service.impl;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
public class RemoteDjConfigurationImpl {
    @Value("${remotedj.callbackurl}")
    private String spotifyCallBack;

    @Value("${remotedj.clientid}")
    private String clientId;

    @Value("${remotedj.clientsecret}")
    private String clientSecret;

}
