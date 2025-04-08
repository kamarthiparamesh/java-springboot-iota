package helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.affinidi.tdk.authProvider.AuthProvider;
import com.affinidi.tdk.authProvider.exception.ConfigurationException;
import com.affinidi.tdk.authProvider.exception.PSTGenerationException;
import com.affinidi.tdk.common.EnvironmentUtil;
import com.affinidi.tdk.common.VaultUtil;
import com.affinidi.tdk.credential.issuance.client.ApiClient;
import com.affinidi.tdk.credential.issuance.client.Configuration;
import com.affinidi.tdk.credential.issuance.client.apis.IssuanceApi;
import com.affinidi.tdk.credential.issuance.client.auth.ApiKeyAuth;
import com.affinidi.tdk.credential.issuance.client.models.StartIssuanceInput;
import com.affinidi.tdk.credential.issuance.client.models.StartIssuanceInput.ClaimModeEnum;
import com.affinidi.tdk.iota.client.apis.IotaApi;
import com.affinidi.tdk.iota.client.models.FetchIOTAVPResponseInput;
import com.affinidi.tdk.iota.client.models.FetchIOTAVPResponseOK;
import com.affinidi.tdk.iota.client.models.InitiateDataSharingRequestInput;
import com.affinidi.tdk.iota.client.models.InitiateDataSharingRequestOK;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.affinidi.tdk.iota.client.models.InitiateDataSharingRequestInput.ModeEnum;
import com.affinidi.tdk.credential.issuance.client.models.StartIssuanceInputDataInner;
import com.affinidi.tdk.credential.issuance.client.models.StartIssuanceResponse;

import com.affinidi.tdk.credential.verification.client.models.VerifyCredentialInput;
import com.affinidi.tdk.credential.verification.client.models.VerifyCredentialOutput;
import com.affinidi.tdk.credential.verification.client.models.W3cCredential;
import com.affinidi.tdk.credential.verification.client.models.W3cCredentialCredentialSchema;
import com.affinidi.tdk.credential.verification.client.models.W3cCredentialCredentialSubject;
import com.affinidi.tdk.credential.verification.client.models.W3cCredentialHolder;
import com.affinidi.tdk.credential.verification.client.models.W3cCredentialStatus;
import com.affinidi.tdk.credential.verification.client.models.W3cPresentationContext;
import com.affinidi.tdk.credential.verification.client.models.W3cProof;
import org.json.JSONObject;

import com.affinidi.tdk.credential.verification.client.apis.DefaultApi;

public class Utils {

    private static AuthProvider authProvider;

    public static AuthProvider getAuthProvider() throws ConfigurationException {
        if (authProvider == null) {
            authProvider = new AuthProvider.Configurations().buildWithEnv();
        }
        return authProvider;
    }

    public static String getAuthorizationToken() throws PSTGenerationException, ConfigurationException {
        System.out.println("\n\n############ Performance Check : Is AuthProvider going to fetch a new token : "
                + getAuthProvider().shouldRefreshToken() + "########## \n\n");
        return getAuthProvider().fetchProjectScopedToken();

    }

    public static StartIssuanceResponse startIssuance(String userDID, Map<String, Object> credentialData,
            String credentialTypeId) {

        StartIssuanceResponse response = null;
        try {
            // Create a client for issuance
            ApiClient issuanceClient = Configuration.getDefaultApiClient();
            ApiKeyAuth issueTokenAuth = (ApiKeyAuth) issuanceClient.getAuthentication("ProjectTokenAuth");

            // Create a authentication token
            String projectToken = getAuthorizationToken();
            issueTokenAuth.setApiKey(projectToken);

            // Iniialize the API client
            IssuanceApi issuanceApi = new IssuanceApi(issuanceClient);

            // Create input for issuance service
            StartIssuanceInput startIssuanceInput = new StartIssuanceInput()
                    .data(new ArrayList<StartIssuanceInputDataInner>(List.of(new StartIssuanceInputDataInner()
                            .credentialTypeId(credentialTypeId).credentialData(credentialData))));

            // Conditionally set claimMode and holderDid
            if (userDID == null || userDID.isEmpty()) {
                startIssuanceInput.claimMode(ClaimModeEnum.TX_CODE);
            } else {
                startIssuanceInput.holderDid(userDID).claimMode(ClaimModeEnum.FIXED_HOLDER);
            }

            // Issue the credential using the data above
            response = issuanceApi.startIssuance(EnvironmentUtil.getValueFromEnvConfig("PROJECT_ID"),
                    startIssuanceInput);
            System.out.println("Credential Offer Generated ********** " + response.getCredentialOfferUri() + "\n\n");

            response.setCredentialOfferUri(VaultUtil.buildClaimLink(response.getCredentialOfferUri()));
            System.out.println("Consumable Vault Claim Link specific to your environment ********** "
                    + response.getCredentialOfferUri());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response;

    }

    public static InitiateDataSharingRequestOK iotaStart(String nonce, String redirectUrl, String iotaQueryId,
            String iotaConfigId) {
        InitiateDataSharingRequestOK response = null;
        try {
            com.affinidi.tdk.iota.client.ApiClient iotaClient = com.affinidi.tdk.iota.client.Configuration
                    .getDefaultApiClient();
            com.affinidi.tdk.iota.client.auth.ApiKeyAuth iotaTokenAuth = (com.affinidi.tdk.iota.client.auth.ApiKeyAuth) iotaClient
                    .getAuthentication("ProjectTokenAuth");

            // Create a authentication token
            String projectToken = getAuthorizationToken();
            iotaTokenAuth.setApiKey(projectToken);

            // Iniialize the API client
            IotaApi iotaApi = new IotaApi(iotaClient);

            // Initiatte IOTA data sharing request
            response = iotaApi.initiateDataSharingRequest(new InitiateDataSharingRequestInput().mode(ModeEnum.REDIRECT)
                    .nonce(nonce).queryId(iotaQueryId).configurationId(iotaConfigId)
                    .correlationId(UUID.randomUUID().toString()).redirectUri(redirectUrl));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response;

    }

    public static FetchIOTAVPResponseOK iotaComplete(String responseCode, String correlationId, String transactionId,
            String iotaConfigId) {

        FetchIOTAVPResponseOK response = null;
        try {

            com.affinidi.tdk.iota.client.ApiClient iotaClient = com.affinidi.tdk.iota.client.Configuration
                    .getDefaultApiClient();
            com.affinidi.tdk.iota.client.auth.ApiKeyAuth iotaTokenAuth = (com.affinidi.tdk.iota.client.auth.ApiKeyAuth) iotaClient
                    .getAuthentication("ProjectTokenAuth");

            // Create a authentication token
            String projectToken = getAuthorizationToken();
            iotaTokenAuth.setApiKey(projectToken);

            // Iniialize the API client
            IotaApi iotaApi = new IotaApi(iotaClient);

            // Create IOTA data sharing request
            response = iotaApi.fetchIotaVpResponse(new FetchIOTAVPResponseInput().transactionId(transactionId)
                    .correlationId(correlationId).responseCode(responseCode).configurationId(iotaConfigId));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response;

    }

    public static String verifyVC(String credentialData) {

        if (credentialData == null || credentialData.isEmpty()) {
            return "Invalid credential data";
        }
        try {
            com.affinidi.tdk.credential.verification.client.ApiClient verificationClient = com.affinidi.tdk.credential.verification.client.Configuration
                    .getDefaultApiClient();

            // Configure API key authorization: ProjectTokenAuth
            com.affinidi.tdk.credential.verification.client.auth.ApiKeyAuth ProjectTokenAuth = (com.affinidi.tdk.credential.verification.client.auth.ApiKeyAuth) verificationClient
                    .getAuthentication("ProjectTokenAuth");
            // Create a authentication token
            String projectToken = getAuthorizationToken();
            ProjectTokenAuth.setApiKey(projectToken);
            System.out.println("Project Token : " + projectToken);

            ObjectMapper mapper = new ObjectMapper();
            // Convert JSON string to W3cCredential object
            Object w3cCredential = mapper.readValue(credentialData, Object.class);
            System.out.println("Credential : " + w3cCredential.toString());


            VerifyCredentialInput verifyCredentialInput = new VerifyCredentialInput();
            verifyCredentialInput.addVerifiableCredentialsItem((W3cCredential) w3cCredential);
            // Initialize the API client
            DefaultApi apiInstance = new DefaultApi(verificationClient);
            VerifyCredentialOutput response = apiInstance.verifyCredentials(verifyCredentialInput);
            System.out.println("Verification response : " + response.toString());
            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Verification failed due to an error.";
    }


}

