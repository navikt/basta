package no.nav.aura.basta.backend.dns.menandmice;

import com.google.common.collect.Lists;
import no.nav.generated.menandmice.ws.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MenAndMiceExecutor {
    private static Logger log = LoggerFactory.getLogger(MenAndMiceExecutor.class);

    private static  QName SERVICE_NAME = new QName("http://menandmice.com/webservices/", "Service");
    private final String serverUrl;

    private String username;
    private String password;
    private URL endpoint;
    private ServiceSoap mmService;

    @Autowired
    public MenAndMiceExecutor(@Value("${ws.menandmice.url}") String url, @Value("${ws.menandmice.username}") String userName, @Value("${ws.menandmice.password}") String password) {
        this.username = userName;
        this.password = password;
        this.endpoint = parseURL(url);
        this.serverUrl = endpoint.getHost();
        this.mmService = new Service(getClass().getResource("/menandmice.wsdl"), SERVICE_NAME).getServiceSoap();
        BindingProvider bp = (BindingProvider) mmService;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
    }


    public List<String> getUsers(String session){
        Holder<ArrayOfUser> usersHolder = new Holder<>();
        mmService.getUsers(session,null,null,null,null,null, usersHolder, null);
        return usersHolder.value.getUser()
                .stream()
                .map(user -> user.getName())
                .filter(navn -> navn.startsWith("PREPROD"))

                .collect(Collectors.toList());

    }

    public List<String> getDnsRecords(String session, String ip) {

        return Lists.newArrayList("johnny.adeo.no", "truls.adeo.no", "tar.et.oppfolgingspunk,paa.denna");
    }

    public String login(){return mmService.login(serverUrl,username, password, null);}


    public void logout(String session){mmService.logout(session);}

    private static URL parseURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException mue) {
            throw new RuntimeException("Error resolving URL " + url, mue);
        }
    }
}