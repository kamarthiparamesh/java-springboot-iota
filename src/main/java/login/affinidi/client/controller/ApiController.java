package login.affinidi.client.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import helpers.Utils;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class ApiController {

    @PostMapping(path = "/cis-issuance", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> issuance(Model model, @RequestBody Map<String, String> data) {
        var userDid = data.get("userDid");
        System.out.println("userDid " + userDid);
        var response = Utils.startIssuance(userDid);
        return response;
    }

    @PostMapping(path = "/iota-init", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> iotaInit(Model model, @RequestBody Map<String, String> data) {
        try {
            var nonce = data.get("nonce");
            var redirectUrl = data.get("redirectUrl");
            var response = Utils.iotaStart(nonce, redirectUrl);
            return response;

        } catch (Exception e) {
            // Log the exception
            e.printStackTrace();

            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return Mono.just(errorResponse);
        }

    }

    @PostMapping(path = "/iota-callback", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> iotaCallback(Model model, @RequestBody Map<String, String> data) {
        try {
            var responseCode = data.get("responseCode");
            var correlationId = data.get("correlationId");
            var transactionId = data.get("transactionId");
            var response = Utils.iotaComplete(responseCode, correlationId, transactionId);
            return response;
        } catch (Exception e) {
            // Log the exception
            e.printStackTrace();

            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return Mono.just(errorResponse);
        }
    }

}
