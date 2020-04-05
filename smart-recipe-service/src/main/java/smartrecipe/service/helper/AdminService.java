package smartrecipe.service.helper;

import org.springframework.stereotype.Service;
import smartrecipe.service.dto.AdminEntityKeysEnum;

@Service
public interface AdminService {
    void checkAndIncrementGoogleAPICall(AdminEntityKeysEnum adminEntityKeysEnum) throws Exception;
}
