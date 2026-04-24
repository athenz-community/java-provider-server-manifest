import com.sun.net.httpserver.HttpExchange;
import com.yahoo.athenz.zpe.AuthZpeClient;

public class Authorizer {

  private final boolean isRequired;
  private static final String DEFAULT_JWK_URI = "https://localhost:8443/zts/v1/oauth2/keys?rfc=true";

  public Authorizer() {
    this.isRequired = Boolean.parseBoolean(System.getenv().getOrDefault("AT_REQUIRED", "true"));

    if (this.isRequired) {
      if (System.getProperty("athenz.zpe.jwk_uri") == null) {
        System.setProperty("athenz.zpe.jwk_uri", DEFAULT_JWK_URI);
      }
      if (System.getProperty("athenz.zpe.policy_dir") == null) {
        System.setProperty("athenz.zpe.policy_dir", "./policies");
      }
      if (System.getProperty("athenz.zpe.check_policy_zms_signature") == null) {
        System.setProperty("athenz.zpe.check_policy_zms_signature", "false");
      }
      AuthZpeClient.init();
    }
  }

  public boolean isRequired() {
    return this.isRequired;
  }

  private String getAction(HttpExchange exchange) {
    String method = exchange.getRequestMethod();

    switch (method.toUpperCase()) {
      case "GET":
        return "get";
      case "POST":
        return "post";
      case "PUT":
        return "put";
      case "PATCH":
        return "patch";
      case "DELETE":
        return "delete";
      default:
        throw new IllegalArgumentException("Invalid HTTP method: " + method);
    }
  }

  public void authorizeRequest(HttpExchange exchange, String resource) {
    if (!this.isRequired) {
      return;
    }

    String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
    String token = (authHeader != null && authHeader.startsWith("Bearer "))
        ? authHeader.substring(7)
        : null;

    if (token == null || token.isEmpty()) {
      throw new IllegalArgumentException("Authorization header is missing or invalid Bearer token.");
    }
    String action = getAction(exchange);
    AuthZpeClient.AccessCheckStatus status = AuthZpeClient.allowAccess(token, resource, action);

    if (status != AuthZpeClient.AccessCheckStatus.ALLOW) {
      throw new SecurityException(String.format("Token does not have '%s' action on '%s'", action, resource));
    }
  }
}