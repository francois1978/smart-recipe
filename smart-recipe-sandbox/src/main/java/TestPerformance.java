import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class TestPerformance {


    public static void main(String args[]) {


        log.info("Trying to reach server");

        log.info("" + System.getenv());
        String clientIP = "192.168.43.16";
        String[] clientIPParts = StringUtils.split(clientIP, ".");


        String startIPPrefix = clientIPParts[0] + "." + clientIPParts[1] + "." + clientIPParts[2] + ".";
        String targetServerIP = null;
        //int i = 0;
        //while (i < 255 && targetServerIP == null) {
        int j = 0;
        while (j < 255 && targetServerIP == null) {
            String hostName = startIPPrefix + j;
            String port = "8081";
            String servceURL = "http://" + hostName + ":" + port + "/sr/";
            RestTemplate restTemplate = new RestTemplate();
            try {
                String status = restTemplate.getForObject(servceURL + "healthcheck/", String.class);
                log.info("Status is OK:" + status  + " and server adress is " + servceURL + ":" + port);
                targetServerIP = hostName;
            } catch (ResourceAccessException e) {
                j++;
            }
        }
        //}
    }
}
