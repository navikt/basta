package no.nav.aura.basta.backend.certificate.ad;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.security.KeyStore;

import javax.ws.rs.core.UriBuilder;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvConfigService {

    private static Logger log = LoggerFactory.getLogger(EnvConfigService.class);
    private String envConfigRestUrl;

    private DefaultHttpClient httpClient;

    public EnvConfigService(String envConfigRestUrl, String username, String password) {
        this.envConfigRestUrl = envConfigRestUrl;
        httpClient = new DefaultHttpClient();
        Credentials credentials = new UsernamePasswordCredentials(username, password);
        httpClient.getCredentialsProvider().setCredentials(org.apache.http.auth.AuthScope.ANY, credentials);
    }

    public void storeApplicationCertificate(ServiceUserAccount user) {
        try {
            HttpPut put = new HttpPut(envConfigRestUrl + "/resources/Certificate");
            MultipartEntity reqEntity = new MultipartEntity();
            if (user.getUserAccountName().endsWith("_u")) {
                reqEntity.addPart("scope.domain", new StringBody("devillo.no"));
            } else {
                reqEntity.addPart("scope.domain", new StringBody(user.getDomain()));
            }

            reqEntity.addPart("scope.application", new StringBody(user.getApplicationName()));
            reqEntity.addPart("scope.environmentclass", new StringBody(user.getEnvironmentClass()));
            reqEntity.addPart("scope.environmentname", new StringBody(""));
            reqEntity.addPart("alias", new StringBody(user.getAlias()));
            reqEntity.addPart("keystore.filename", new StringBody("keystore.jks"));
            reqEntity.addPart("keystore.file", new ByteArrayBody(getStream(user.getKeyStore(), user.getKeyStorePassword()), "keystore.jks"));
            reqEntity.addPart("keystorepassword", new StringBody(user.getKeyStorePassword()));
            reqEntity.addPart("keystorealias", new StringBody(user.getKeyStoreAlias()));

            put.setEntity(reqEntity);
            HttpResponse response = httpClient.execute(put);
            verifyResponse(response, put);
            EntityUtils.consumeQuietly(response.getEntity());
            log.info("Stored certificate with alias {} in envconfig", user.getAlias());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void verifyResponse(HttpResponse response, HttpRequestBase request) throws IOException {
        if (response.getStatusLine().getStatusCode() >= 400) {
            throw new RuntimeException("Error in response from  " + request.getMethod() + ":" + request.getURI() + " Http status: " + response.getStatusLine() + "\n " + EntityUtils.toString(response.getEntity()));
        }

    }

    private byte[] getStream(KeyStore keyStore, String password) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            keyStore.store(out, password.toCharArray());
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void storeCredential(ServiceUserAccount userAccount) {
        try {
            HttpPut put = new HttpPut(envConfigRestUrl + "/resources/Credential");
            MultipartEntity reqEntity = new MultipartEntity();
            if (userAccount.getUserAccountName().endsWith("_u")) {
                reqEntity.addPart("scope.domain", new StringBody("devillo.no"));
            } else {
                reqEntity.addPart("scope.domain", new StringBody(userAccount.getDomain()));
            }
            reqEntity.addPart("scope.environmentclass", new StringBody(userAccount.getEnvironmentClass()));
            reqEntity.addPart("scope.application", new StringBody(userAccount.getApplicationName()));
            reqEntity.addPart("scope.environmentname", new StringBody(""));
            reqEntity.addPart("alias", new StringBody(userAccount.getAlias()));
            reqEntity.addPart("username", new StringBody(userAccount.getUserAccountName()));
            reqEntity.addPart("password", new StringBody(userAccount.getPassword()));

            put.setEntity(reqEntity);
            HttpResponse response = httpClient.execute(put);
            verifyResponse(response, put);
            EntityUtils.consumeQuietly(response.getEntity());
            log.info("Stored credential with alias {} in envconfig", userAccount.getAlias());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean resourceExists(ServiceUserAccount userAccount, String resourceType) {
        try {
            UriBuilder builder;
            if (userAccount.getUserAccountName().endsWith("_u")) {
                builder = UriBuilder.fromPath(envConfigRestUrl).path("resources").queryParam("envClass", userAccount.getEnvironmentClass()).queryParam("domain", "devillo.no")
                        .queryParam("app", userAccount.getApplicationName()).queryParam("alias", userAccount.getAlias()).queryParam("type", resourceType);
            } else {
                builder = UriBuilder.fromPath(envConfigRestUrl).path("resources").queryParam("envClass", userAccount.getEnvironmentClass()).queryParam("domain", userAccount.getDomain())
                        .queryParam("app", userAccount.getApplicationName()).queryParam("alias", userAccount.getAlias()).queryParam("type", resourceType);
            }

            URI url = builder.build();
            log.info("calling " + url);
            HttpGet get = new HttpGet(url);
            httpClient.setCookieStore(new BasicCookieStore());
            HttpResponse response = httpClient.execute(get);
            verifyResponse(response, get);
            HttpEntity entity = response.getEntity();

            String content = EntityUtils.toString(entity);
            EntityUtils.consumeQuietly(entity);
            boolean resourceExists = content.contains("<alias>" + userAccount.getAlias() + "</alias>");
            log.debug("resource with of type {} alias {} exist: ", resourceType, userAccount.getAlias(), resourceExists);
            return resourceExists;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
