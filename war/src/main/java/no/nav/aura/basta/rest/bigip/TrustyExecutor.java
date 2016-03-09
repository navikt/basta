package no.nav.aura.basta.rest.bigip;

import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class TrustyExecutor extends ApacheHttpClient4Executor {
    public static TrustyExecutor getTrustingExecutor() {
        TrustyExecutor executor = new TrustyExecutor();

        TrustStrategy trustStrategy = new TrustStrategy() {
            @Override
            public boolean isTrusted(java.security.cert.X509Certificate[] chain, String authType)
                    throws CertificateException {
                return true;
            }
        };

        SSLSocketFactory factory = null;
        try {
            factory = new SSLSocketFactory(trustStrategy, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e1) {
            e1.printStackTrace();
        }
        Scheme https = new Scheme("https", 443, factory);

        executor.getHttpClient().getConnectionManager().getSchemeRegistry().register(https);
        return executor;
    }
}
