package helpers;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.affinidi.tdk.authprovider.*;
import io.github.cdimascio.dotenv.Dotenv;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.util.UUID;

public class Utils {
    private static Dotenv dotenv;

    private static WebClient webClient;

    static {
        dotenv = Dotenv.load();
        webClient = WebClient.builder()
                .baseUrl(dotenv.get("API_GATEWAY_URL"))
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    private static AuthProvider getAuthProvider() {
        Map<String, String> params = new HashMap<>();
        params.put("projectId", dotenv.get("PROJECT_ID"));
        params.put("tokenId", dotenv.get("TOKEN_ID"));
        params.put("keyId", dotenv.get("KEY_ID"));
        params.put("privateKey", dotenv.get("PRIVATE_KEY").replace("\\n", System.getProperty("line.separator")));
        params.put("passphrase", dotenv.get("PASSPHRASE"));

        // Create an instance of AuthProvider
        AuthProvider authProvider = new AuthProvider(params);
        return authProvider;
    }

    public static String GeneratePST() {

        // Create an instance of AuthProvider
        AuthProvider authProvider = getAuthProvider();

        // Call fetchProjectScopedToken method
        String projectScopedToken = authProvider.fetchProjectScopedToken();

        // Output the token
        System.out.println("Project Scoped Token: " + projectScopedToken);

        return projectScopedToken;
    }

    public static String getIotaJWT(String userDID) throws Exception {

        AuthProvider authProvider = getAuthProvider();
        var iotaConfigId = dotenv.get("IOTA_CONFIG_ID");

        // Call fetchProjectScopedToken method
        IotaTokenOutput tokenOutput = authProvider.createIotaToken(iotaConfigId, userDID);

        var jwt = tokenOutput.getIotaJwt();
        var sessionId = tokenOutput.getIotaSessionId();
        System.out.println("Iota JWT: " + jwt);
        System.out.println("Iota sessionId: " + sessionId);

        return tokenOutput.getIotaJwt();
    }

    public static Mono<Map<String, Object>> startIssuance(String userDID) {

        String apiEndpoint = String.format("/cis/v1/%s/issuance/start", dotenv.get("PROJECT_ID"));

        Map<String, Object> credentialData = Map.of(
                "email", "paramesh.k@afffinid.com",
                "name", "parmaesh",
                "phoneNumber", "998016607",
                "dob", "22/02/2010",
                "gender", "Male",
                "address", "Bangalore",
                "postcode", "560103",
                "city", "Bangalore",
                "country", "India");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("data", List.of(Map.of(
                "credentialTypeId", "InsuranceRegistration",
                "credentialData", credentialData)));

        // Conditionally set claimMode and holderDid
        if (userDID == null || userDID.isEmpty()) {
            requestBody.put("claimMode", "TX_CODE");
        } else {
            requestBody.put("claimMode", "FIXED_HOLDER");
            requestBody.put("holderDid", userDID);
        }

        var projectScopedToken = GeneratePST();
        var headers = Map.of("Authorization", String.format("Bearer %s", projectScopedToken));

        var response = sendPostRequest(apiEndpoint, headers, requestBody);

        System.out.println("Request Successful, response: " + response);

        return response;

    }

    public static Mono<Map<String, Object>> iotaStart(String nonce, String redirectUrl) {

        String apiEndpoint = "/ais/v1/initiate-data-sharing-request";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("configurationId", dotenv.get("IOTA_CONFIG_ID"));
        requestBody.put("mode", "redirect");
        requestBody.put("queryId", dotenv.get("IOTA_QUERY_ID"));
        requestBody.put("correlationId", UUID.randomUUID().toString());
        requestBody.put("nonce", nonce);
        requestBody.put("redirectUri", redirectUrl);

        var projectScopedToken = GeneratePST();
        var headers = Map.of("Authorization", String.format("Bearer %s", projectScopedToken));

        var response = sendPostRequest(apiEndpoint, headers, requestBody);

        System.out.println("Request Successful, response: " + response);

        return response;

    }

    public static Mono<Map<String, Object>> iotaComplete(String responseCode, String correlationId,
            String transactionId) {

        String apiEndpoint = "/ais/v1/fetch-iota-response";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("configurationId", dotenv.get("IOTA_CONFIG_ID"));
        requestBody.put("responseCode", responseCode);
        requestBody.put("correlationId", correlationId);
        requestBody.put("transactionId", transactionId);

        var projectScopedToken = GeneratePST();
        var headers = Map.of("Authorization", String.format("Bearer %s", projectScopedToken));

        var response = sendPostRequest(apiEndpoint, headers, requestBody);

        return response;

    }

    private static Mono<Map<String, Object>> sendPostRequest(String apiEndpoint, Map<String, String> headers,
            Object requestBody) {
        return webClient.post()
                .uri(apiEndpoint)
                .headers(httpHeaders -> {
                    if (headers != null) {
                        headers.forEach(httpHeaders::set);
                    }
                })
                .body(Mono.just(requestBody), Object.class)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .onErrorResume(WebClientResponseException.class, e -> {
                    System.err.println("WebClientResponseException: " + e.getResponseBodyAsString());
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("status", "error");
                    errorResponse.put("message", e.getMessage());
                    errorResponse.put("responseBody", e.getResponseBodyAsString());
                    return Mono.just(errorResponse);
                });
    }

}
