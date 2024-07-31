package login.affinidi.client.controller;

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

}
