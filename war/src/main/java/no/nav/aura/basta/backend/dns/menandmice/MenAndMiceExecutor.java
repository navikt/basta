package no.nav.aura.basta.backend.dns.menandmice;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import no.nav.generated.menandmice.ws.ArrayOfUser;
import no.nav.generated.menandmice.ws.Service;
import no.nav.generated.menandmice.ws.ServiceSoap;

public class MenAndMiceExecutor {
    private static Logger log = LoggerFactory.getLogger(MenAndMiceExecutor.class);

    private static  QName SERVICE_NAME = new QName("http://menandmice.com/webservices/", "Service");
    private final String serverUrl;

    private String username;
    private String password;
    private URL endpoint;
    private ServiceSoap mmService;

    public MenAndMiceExecutor( String url,  String userName,  String password) {
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
        return Arrays.asList("johnny.adeo.no", "truls.adeo.no", "tar.et.oppfolgingspunk,paa.denna");
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