package no.nav.aura.basta.domain.input.vm;

import no.nav.aura.envconfig.client.PlatformTypeDO;

public class Converters {

    public static PlatformTypeDO platformTypeDOFrom(NodeType nodeType) {
        switch (nodeType) {
        case BPM_NODES:
        case BPM_DEPLOYMENT_MANAGER:
            return PlatformTypeDO.BPM;
        case BPM9_NODES:
        case BPM9_DEPLOYMENT_MANAGER:
            return PlatformTypeDO.BPM9;
        case WILDFLY:
            return PlatformTypeDO.JBOSS;
        case JBOSS:
            return PlatformTypeDO.JBOSS;
        case OPENAM_SERVER:
            return PlatformTypeDO.OPENAM_SERVER;
        case OPENAM_PROXY:
            return PlatformTypeDO.OPENAM_PROXY;
        case WAS_DEPLOYMENT_MANAGER:
        case WAS_NODES:
            return PlatformTypeDO.WAS;
        case WAS9_DEPLOYMENT_MANAGER:
        case WAS9_NODES:
            return PlatformTypeDO.WAS9;
        case LIBERTY:
            return PlatformTypeDO.LIBERTY;
        default:
            throw new IllegalArgumentException("No fasit platform type for node type " + nodeType);
        }
    }

}
