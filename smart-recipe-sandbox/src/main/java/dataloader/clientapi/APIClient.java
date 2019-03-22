package dataloader.clientapi;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class APIClient {
    protected static String SERVICE_URL;

    static {
        SERVICE_URL = "http://" + System.getProperty("SERVICE_URL") + "/sr/";
        log.info("Service URL: " + SERVICE_URL);
    }
}
