package no.nav.aura.basta.domain.input.vm;

import no.nav.aura.envconfig.client.PlatformTypeDO;

public class Converters {

    @Deprecated // Don't use fasit client api, use rest client instead
    public static PlatformTypeDO platformTypeDOFrom(NodeType nodeType) {
        switch (nodeType) {
        case BPM_NODES:
        case BPM_DEPLOYMENT_MANAGER:
            return PlatformTypeDO.BPM;
        case BPM86_NODES:
        //case BPM86_DEPLOYMENT_MANAGER:
            //return PlatformTypeDO.BPM86;
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

    public static String fasitPlatformTypeFrom(NodeType nodeType) {
        switch (nodeType) {
            case BPM_NODES:
            case BPM_DEPLOYMENT_MANAGER:
                return "bpm";
            case BPM86_NODES:
            case BPM86_DEPLOYMENT_MANAGER:
                return "bpm86";
            case DOCKERHOST:
                return "docker";
            case WILDFLY:
            case JBOSS:
                return "jboss";
            case OPENAM_SERVER:
                return "openam_server";
            case OPENAM_PROXY:
                return "openam_proxy";
            case WAS_DEPLOYMENT_MANAGER:
            case WAS_NODES:
                return "was";
            case WAS9_DEPLOYMENT_MANAGER:
            case WAS9_NODES:
                return "was9";
            case LIBERTY:
                return "liberty";
            default:
                throw new IllegalArgumentException("No fasit platform type for node type " + nodeType);
        }
    }

}
