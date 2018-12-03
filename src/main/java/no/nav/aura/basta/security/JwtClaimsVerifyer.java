package no.nav.aura.basta.security;


import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import org.springframework.beans.factory.annotation.Value;

public class JwtClaimsVerifyer<C extends SecurityContext> extends DefaultJWTClaimsVerifier<C> {

    // prod
/*    private static final String BASTA_CLIENT_ID = "9733f92e-fa66-4589-8067-b91e5c482390";
    private static final String BASTA_FRONTEND_APP_ID = "9733f92e-fa66-4589-8067-b91e5c482390";
    private static final String TENANT_ID = "62366534-1ec3-4962-8869-9b5535279d0b";
    private static final String TOKEN_ISSUER = "https://sts.windows.net/62366534-1ec3-4962-8869-9b5535279d0b/";*/
    // dev

    private static final String BASTA_CLIENT_ID = System.getProperty("basta_client_id");
    private static final String BASTA_FRONTEND_APP_ID = System.getProperty("basta_frontend_app_id");
    private static final String TENANT_ID = System.getProperty("tenant_id");
    private static final String TOKEN_ISSUER = System.getProperty("token_issuer");

    private static final BadJWTException MISSING_TOKEN_EXPIRATION_EXCEPTION = new BadJWTException("Missing token expiration claim");
    private static final BadJWTException INVALID_AUDIENCE_EXCEPTION = new BadJWTException("Invalid audience");
    private static final BadJWTException APP_ID_NOT_AUTHORIZED_EXCEPTION = new BadJWTException("AppId not authorized to call this application");
    private static final BadJWTException PUBLIC_CLIENTS_NOT_ALLOWED_EXCEPTION = new BadJWTException("Public clients are not allowed to call this API");
    private static final BadJWTException INVALID_TENANT_ID_EXCEPTION = new BadJWTException("Tenant is not allowed to call this API");
    private static final BadJWTException UNTRUSTED_ISSUER_EXCEPTION = new BadJWTException("Tokes is issued by untrusted issuer");


    public JwtClaimsVerifyer() {
        super();
        System.out.println("BCI " + BASTA_CLIENT_ID);
        System.out.println("BFAI " + BASTA_FRONTEND_APP_ID);
        System.out.println("TI " + TENANT_ID);
        System.out.println("TOIS " + TOKEN_ISSUER);
    }


    @Override
    public void verify(JWTClaimsSet claimsSet, C context) throws BadJWTException {
        super.verify(claimsSet, context);

        if (claimsSet.getExpirationTime() == null) {
            throw MISSING_TOKEN_EXPIRATION_EXCEPTION;
        }

        for (String audience : claimsSet.getAudience()) {
            if (!audience.equals(BASTA_CLIENT_ID)) {
                throw INVALID_AUDIENCE_EXCEPTION;
            }
        }

        if (!claimsSet.getClaim("appid").equals(BASTA_FRONTEND_APP_ID)) {
            throw APP_ID_NOT_AUTHORIZED_EXCEPTION;
        }

        if (claimsSet.getClaim("appidacr").equals("0")) {
            throw PUBLIC_CLIENTS_NOT_ALLOWED_EXCEPTION;
        }

        if (!claimsSet.getClaim("tid").equals(TENANT_ID)) {
            throw INVALID_TENANT_ID_EXCEPTION;
        }


        if (!claimsSet.getIssuer().equals(TOKEN_ISSUER)) {
            throw UNTRUSTED_ISSUER_EXCEPTION;
        }
    }
}
