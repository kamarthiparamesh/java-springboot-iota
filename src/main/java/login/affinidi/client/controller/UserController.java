package login.affinidi.client.controller;

import java.util.ArrayList;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.nimbusds.jose.shaded.gson.internal.LinkedTreeMap;

@Controller
public class UserController {

    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser != null) {
            var userDid = oidcUser.getSubject();
            System.out.println("userDid " + userDid);
            model.addAttribute("userDid", userDid);
        }
        return "index";
    }

    /**
     * This method acts as handler for /user endpoint. It extracs details from
     * authenticator oidc user for user interface
     *
     * @param model
     * @param oidcUser
     * @return
     */
    @GetMapping("/user")
    public String user(Model model, @AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser != null) {
            var userDid = oidcUser.getSubject();
            System.out.println("userDid " + userDid);
            model.addAttribute("userDid", userDid);
        }
        if (oidcUser != null) {
            @SuppressWarnings("unchecked")
            ArrayList<LinkedTreeMap<String, Object>> customNodeFromToken = (ArrayList<LinkedTreeMap<String, Object>>) oidcUser
                    .getAttributes().get("custom");
            populateModel(customNodeFromToken, model);
        }
        return "user";
    }

    @GetMapping("/issuance")
    public String issuance(Model model, @AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser != null) {
            var userDid = oidcUser.getSubject();
            System.out.println("userDid " + userDid);
            model.addAttribute("userDid", userDid);
        }
        return "issuance";
    }

    @GetMapping("/iota")

    public String iota(Model model, @AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser != null) {
            var userDid = oidcUser.getSubject();
            System.out.println("userDid " + userDid);
            model.addAttribute("userDid", userDid);
        }
        return "iota";
    }

    @GetMapping("/verify")
    public String verify(Model model, @AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser != null) {
            var userDid = oidcUser.getSubject();
            System.out.println("userDid " + userDid);
            model.addAttribute("userDid", userDid);
        }
        return "verify";
    }

    /**
     * This method extracts every populated attribute from custom node of idToken
     * and adds it to UI model for display
     *
     * @param customNodeFromToken
     * @param model
     */
    private void populateModel(ArrayList<LinkedTreeMap<String, Object>> customNodeFromToken, Model model) {
        for (LinkedTreeMap<String, Object> eachAttribute : customNodeFromToken) {
            if (eachAttribute != null) {
                for (String key : eachAttribute.keySet()) {
                    model.addAttribute(key, eachAttribute.get(key));
                }
            }
        }
    }
}
