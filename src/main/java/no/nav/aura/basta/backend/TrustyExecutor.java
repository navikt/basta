package no.nav.aura.basta.backend;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient43Engine;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;

public class TrustyExecutor extends ApacheHttpClient43Engine  {
//    public static TrustyExecutor getTrustingExecutor() {
//        TrustyExecutor executor = new TrustyExecutor();
//
//        TrustStrategy trustStrategy = new TrustStrategy() {
//            @Override
//            public boolean isTrusted(java.security.cert.X509Certificate[] chain, String authType)
//                    throws CertificateException {
//                return true;
//            }
//        };
//
//        SSLSocketFactory factory = null;
//
//        try {
//            factory = new SSLSocketFactory(trustStrategy, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
//        } catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e1) {
//            e1.printStackTrace();
//        }
//        Scheme https = new Scheme("https", 443, factory);
//
//        executor.getHttpClient().getConnectionManager().getSchemeRegistry().register(https);
//        return executor;
//    }
	   public static TrustyExecutor getTrustingExecutor() {
	        try {
	            SSLContext sslContext = new SSLContextBuilder()
	                    .loadTrustMaterial(null, (chain, authType) -> true)
	                    .build();

	            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

	            CloseableHttpClient httpClient = HttpClients.custom()
	                    .setSSLSocketFactory(sslsf)
	                    .build();

	            return new TrustyExecutor(httpClient);
	        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
	            throw new RuntimeException("Failed to create TrustyExecutor", e);
	        }
	    }
	   
    private TrustyExecutor(CloseableHttpClient httpClient) {
        super(httpClient);
    }
}
