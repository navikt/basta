package no.nav.aura.basta.security;


import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;

public class JwtClaimsVerifyer<C extends SecurityContext> extends DefaultJWTClaimsVerifier<C> {

    private static final String BASTA_CLIENT_ID = "b36e92f3-d48b-473d-8f69-e7887457bd3f";
    private static final String BASTA_FRONTEND_APP_ID = "b36e92f3-d48b-473d-8f69-e7887457bd3f";
    private static final String TENANT_ID = "966ac572-f5b7-4bbe-aa88-c76419c0f851";
    private static final String ISSUER = "https://sts.windows.net/966ac572-f5b7-4bbe-aa88-c76419c0f851/";

    private static final BadJWTException MISSING_TOKEN_EXPIRATION_EXCEPTION = new BadJWTException("Missing token expiration claim");
    private static final BadJWTException INVALID_AUDIENCE_EXCEPTION = new BadJWTException("Invalid audience");
    private static final BadJWTException APP_ID_NOT_AUTHORIZED_EXCEPTION = new BadJWTException("AppId not authorized to call this application");
    private static final BadJWTException PUBLIC_CLIENTS_NOT_ALLOWED_EXCEPTION = new BadJWTException("Public clients are not allowed to call this API");
    private static final BadJWTException INVALID_TENANT_ID_EXCEPTION = new BadJWTException("Tenant is not allowed to call this API");
    private static final BadJWTException UNTRUSTED_ISSUER_EXCEPTION = new BadJWTException("Tokes is issued by untrusted issuer");


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
            if(!audience.equals(BASTA_CLIENT_ID)) {
                throw INVALID_AUDIENCE_EXCEPTION;
            }
        }

        if(!claimsSet.getClaim("appid").equals(BASTA_FRONTEND_APP_ID)) {
            throw APP_ID_NOT_AUTHORIZED_EXCEPTION;
        }

        if(claimsSet.getClaim("appidacr").equals("0"))  {
            throw PUBLIC_CLIENTS_NOT_ALLOWED_EXCEPTION;
        }

        if(!claimsSet.getClaim("tid").equals(TENANT_ID)) {
            throw INVALID_TENANT_ID_EXCEPTION;
        }


        if(!claimsSet.getIssuer().equals(ISSUER)) {
            throw UNTRUSTED_ISSUER_EXCEPTION;
        }
    }
}
