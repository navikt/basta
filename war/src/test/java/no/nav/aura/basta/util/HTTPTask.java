package no.nav.aura.basta.util;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;

import javax.ws.rs.core.MediaType;
import java.net.URI;

/**
* Created with IntelliJ IDEA.
* User: j116592
* Date: 27.08.14
* Time: 13:33
* To change this template use File | Settings | File Templates.
*/
public class HTTPTask implements Runnable {


    private final URI uri;
    private final Object xmldata;
    private final HTTPOperation httpOperation;

    public HTTPTask(URI uri, Object xmldata, HTTPOperation httpOperation) {
        this.uri = uri;
        this.xmldata = xmldata;
        this.httpOperation = httpOperation;
    }

    public void run() {
        RestEasyDetails bee = new RestEasyDetails("", "");
        try {
            Thread.sleep(3000);
            ClientRequest request = bee.createClientRequest(uri).body(MediaType.APPLICATION_XML_TYPE, xmldata);
            ClientResponse response = bee.performHttpOperation(request, httpOperation);
            response.releaseConnection();

        } catch (Exception e) {
            e.printStackTrace();
    }


    }

}
