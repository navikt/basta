package no.nav.aura.basta.domain.input.vm;

import no.nav.aura.basta.backend.fasit.payload.PlatformType;
import no.nav.aura.envconfig.client.PlatformTypeDO;

public class Converters {

    @Deprecated // Don't use fasit client api, use rest client instead
    public static PlatformTypeDO platformTypeDOFrom(NodeType nodeType) {
        switch (nodeType) {
        case BPM_NODES:
        case BPM_DEPLOYMENT_MANAGER:
            return PlatformTypeDO.BPM;
        case BPM86_NODES:
        case BPM86_DEPLOYMENT_MANAGER:
            return PlatformTypeDO.BPM86;
        case WILDFLY:
            return PlatformTypeDO.JBOSS;
        case JBOSS:
            return PlatformTypeDO.JBOSS;
        case WAS_DEPLOYMENT_MANAGER:
        case WAS_NODES:
            return PlatformTypeDO.WAS;
        case WAS9_DEPLOYMENT_MANAGER:
        case WAS9_NODES:
            return PlatformTypeDO.WAS9;
        default:
            throw new IllegalArgumentException("No fasit platform type for node type " + nodeType);
        }
    }

    public static PlatformType fasitPlatformTypeEnumFrom(NodeType nodeType) {
        switch (nodeType) {
            case BPM_NODES:
            case BPM_DEPLOYMENT_MANAGER:
                return PlatformType.BPM;
            case BPM86_NODES:
            case BPM86_DEPLOYMENT_MANAGER:
                return PlatformType.BPM86;
            case WILDFLY:
            case JBOSS:
                return PlatformType.JBOSS;
            case WAS_DEPLOYMENT_MANAGER:
            case WAS_NODES:
                return PlatformType.WAS;
            case WAS9_DEPLOYMENT_MANAGER:
            case WAS9_NODES:
                return PlatformType.WAS9;
            default:
                throw new IllegalArgumentException("No fasit platform type for node type " + nodeType);
        }
    }
}
