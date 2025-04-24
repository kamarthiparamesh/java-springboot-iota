package login.affinidi.client.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.affinidi.tdk.common.EnvironmentUtil;
import com.affinidi.tdk.credential.issuance.client.models.StartIssuanceResponse;
import com.affinidi.tdk.iota.client.models.FetchIOTAVPResponseOK;
import com.affinidi.tdk.iota.client.models.InitiateDataSharingRequestOK;

import helpers.Utils;

@RestController
@RequestMapping("/api")
public class ApiController {

    @PostMapping(path = "/cis-issuance", produces = MediaType.APPLICATION_JSON_VALUE)
    public StartIssuanceResponse issuance(Model model, @RequestBody Map<String, String> data) {
        var userDid = data.get("userDid");
        System.out.println("userDid " + userDid);

        var IssueType = data.get("issueType");
        System.out.println("IssueType " + IssueType);

        ArrayList<Map<String, Object>> credentialRequest = new ArrayList<>();
        String credentialTypeId = "InsuranceRegistration";

        if ("batch".equals(IssueType)) {
            // Construct batch request with multiple credentials
            credentialRequest.add(Map.of(
                    "credentialTypeId", credentialTypeId,
                    "credentialData", Map.of(
                            "email", "user1@example.com",
                            "name", "User One",
                            "phoneNumber", "1234567890",
                            "dob", "01/01/1990",
                            "gender", "Female",
                            "address", "City A",
                            "postcode", "123456",
                            "city", "City A",
                            "country", "Country A"
                    )
            ));

            credentialRequest.add(Map.of(
                    "credentialTypeId", credentialTypeId,
                    "credentialData", Map.of(
                            "email", "user2@example.com",
                            "name", "User Two",
                            "phoneNumber", "0987654321",
                            "dob", "02/02/1992",
                            "gender", "Male",
                            "address", "City B",
                            "postcode", "654321",
                            "city", "City B",
                            "country", "Country B"
                    )
            ));
        } else {
            // Construct single request
            credentialRequest.add(Map.of(
                    "credentialTypeId", credentialTypeId,
                    "credentialData", Map.of(
                            "email", "paramesh.k@afffinid.com",
                            "name", "parmaesh",
                            "phoneNumber", "998016607",
                            "dob", "22/02/2010",
                            "gender", "Male",
                            "address", "Bangalore",
                            "postcode", "560103",
                            "city", "Bangalore",
                            "country", "India"
                    )
            ));
        }

        var response = (StartIssuanceResponse) Utils.startIssuance(userDid, credentialRequest);
        return response;
    }

    @PostMapping(path = "/iota-init", produces = MediaType.APPLICATION_JSON_VALUE)
    public InitiateDataSharingRequestOK iotaInit(Model model, @RequestBody Map<String, String> data) {
        var nonce = data.get("nonce");
        var redirectUrl = data.get("redirectUrl");
        String iotaQueryId = EnvironmentUtil.getValueFromEnvConfig("IOTA_QUERY_ID");
        String iotaConfigId = EnvironmentUtil.getValueFromEnvConfig("IOTA_CONFIG_ID");

        var response = Utils.iotaStart(nonce, redirectUrl, iotaQueryId, iotaConfigId);
        return response;
    }

    @PostMapping(path = "/iota-callback", produces = MediaType.APPLICATION_JSON_VALUE)
    public FetchIOTAVPResponseOK iotaCallback(Model model, @RequestBody Map<String, String> data) {
        var responseCode = data.get("responseCode");
        var correlationId = data.get("correlationId");
        var transactionId = data.get("transactionId");
        String iotaConfigId = EnvironmentUtil.getValueFromEnvConfig("IOTA_CONFIG_ID");
        var response = Utils.iotaComplete(responseCode, correlationId, transactionId, iotaConfigId);
        return response;
    }

    @PostMapping(path = "/vc-verify", produces = MediaType.APPLICATION_JSON_VALUE)
    public String verifyVC(Model model, @RequestBody Map<String, String> data) {
        var credentialData = data.get("credentialData");
        var response = Utils.verifyVC(credentialData);
        return response;
    }

    @PostMapping(path = "/get-claimed-credentials", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getClaimedCredentials(Model model, @RequestBody Map<String, String> data) {
        var issuanceId = data.get("issuanceId");
        var response = Utils.getClaimedCredentials(issuanceId);
        return response;
    }


}
