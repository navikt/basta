package no.nav.aura.basta.domain.input.vm;

import no.nav.aura.basta.backend.fasit.rest.model.infrastructure.PlatformType;

public class Converters {

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
