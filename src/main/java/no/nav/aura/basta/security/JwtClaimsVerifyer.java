package no.nav.aura.basta.security;


import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;

public class JwtClaimsVerifyer<C extends SecurityContext> extends DefaultJWTClaimsVerifier<C> {
    private static final String BASTA_CLIENT_ID = System.getenv("AZURE_APP_CLIENT_ID");
    private static final String BASTA_FRONTEND_APP_ID = System.getenv("BASTA_FRONTEND_APP_ID");
    private static final String TENANT_ID = System.getenv("TENANT_ID");
    private static final String TOKEN_ISSUER = System.getenv("TOKEN_ISSUER");

    private static final BadJWTException MISSING_TOKEN_EXPIRATION_EXCEPTION = new BadJWTException("Missing token expiration claim");
    private static final BadJWTException INVALID_AUDIENCE_EXCEPTION = new BadJWTException("Invalid audience");
    private static final BadJWTException APP_ID_NOT_AUTHORIZED_EXCEPTION = new BadJWTException("AppId not authorized to call this application");
    private static final BadJWTException PUBLIC_CLIENTS_NOT_ALLOWED_EXCEPTION = new BadJWTException("Public clients are not allowed to call this API");
    private static final BadJWTException INVALID_TENANT_ID_EXCEPTION = new BadJWTException("Tenant is not allowed to call this API");
    private static final BadJWTException UNTRUSTED_ISSUER_EXCEPTION = new BadJWTException("Token is issued by untrusted issuer");

    public JwtClaimsVerifyer() {
        super();
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

        if (!BASTA_FRONTEND_APP_ID.equals(claimsSet.getClaim("azp"))) {
            throw APP_ID_NOT_AUTHORIZED_EXCEPTION;
        }

        if ("0".equals(claimsSet.getClaim("azpacr"))) {
            throw PUBLIC_CLIENTS_NOT_ALLOWED_EXCEPTION;
        }

        if (!TENANT_ID.equals(claimsSet.getClaim("tid"))) {
            throw INVALID_TENANT_ID_EXCEPTION;
        }


        if (!TOKEN_ISSUER.equals(claimsSet.getIssuer())) {
            throw UNTRUSTED_ISSUER_EXCEPTION;
        }
    }
}
