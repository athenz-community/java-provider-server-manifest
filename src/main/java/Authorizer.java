import com.yahoo.athenz.zpe.AuthZpeClient;
import com.yahoo.athenz.zpe.ZpeMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Authorizer {
    private static final Logger log = LoggerFactory.getLogger(Authorizer.class);

    public Authorizer() {
        AuthZpeClient.init();
    }

    public boolean isAuthorized(String action, String resource, String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            log.error("Access Token is missing");
            return false;
        }

        ZpeMatch match = AuthZpeClient.allowAccess(accessToken, resource, action);
        
        boolean allowed = (match == ZpeMatch.ALLOW);
        if (!allowed) {
            log.warn("Authorization failed for resource: {}, action: {}", resource, action);
        }
        return allowed;
    }
}