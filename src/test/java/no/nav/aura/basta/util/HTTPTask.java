package no.nav.aura.basta.util;

import java.net.URI;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;

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
        try {
            Thread.sleep(3000);
            
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(this.uri);
            
            Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Basic " + encodeCredentials("user", "user"))
                .header("Accept-Encoding", "");
        
            
            Response response = null;
            
            switch (this.httpOperation.toString().toLowerCase()) {
				case "get": {
					response = builder.get();
					break;
				}
				case "post": {
					response = builder.post(Entity.xml(xmldata));
					break;
				}
                case "put":
                    response = builder.put(Entity.xml(xmldata));
                    break;
                case "delete":
                    response = builder.delete();
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported HTTP operation: " + httpOperation);
			}
            		
            
            response.close();
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String encodeCredentials(String username, String password) {
        byte[] credentials = (username + ':' + password).getBytes();
        return new String(Base64.encodeBase64(credentials));
    }
}
