package no.nav.aura.basta.backend.vmware.orchestrator;

import com.google.gson.Gson;
import no.nav.aura.appconfig.security.Saml;
import no.nav.aura.basta.backend.RestClient;
import no.nav.aura.basta.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OrchestratorClient {
    private RestClient restClient;

    @Autowired
    public OrchestratorClient(@Value("${user.orchestrator.username}") String orcUsername, @Value("${user.orchestrator.password}") String orcPassword) {
        this.restClient = new RestClient(orcUsername, orcPassword);
    }


    public void provisionVM(URL orchestratorUrl, String xmlRequest) {
        String payload = String.format("{\"parameters\": [{\"name\": \"XmlRequest\",\"type\": \"string\",\"value\": {\"string\": {\"value\":%s}} }]}", xmlRequest);
        System.out.println("xmlRequest = " + xmlRequest);
        System.out.println("payload = " + payload);
        Response response = restClient.post(orchestratorUrl.toString(), payload);
        System.out.println("response = " + response);
    }
}

//class ProvisionRequest {
//    List<Parameters> parameters = new ArrayList<>();
//}
//
//class Parameters {
//    String name;
//    String type;
//    Map<String, Map<String, String>> value;
//    Parameters(String name, String type, String xmlRequest){
//        this.name = name;
//        this.type = type;
//        this.value = new HashMap<>();
//        HashMap<String, String> value2= new HashMap<>();
//        value2.put("value", xmlRequest);
//        this.value.put("string", value2);
//    }
//}
