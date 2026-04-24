import com.yahoo.athenz.zpe.AuthZpeClient;

public class Authorizer {

  public Authorizer() {
    AuthZpeClient.init();
  }

  public void authorize(String action, String resource, String accessToken) {
    if (accessToken == null || accessToken.isEmpty()) {
      throw new IllegalArgumentException("Authorization header is missing or invalid Bearer token.");
    }

    AuthZpeClient.AccessCheckStatus status = AuthZpeClient.allowAccess(accessToken, resource, action);

    if (status != AuthZpeClient.AccessCheckStatus.ALLOW) {
      throw new SecurityException(String.format("Token does not have '%s' action on '%s'", action, resource));
    }
  }
}