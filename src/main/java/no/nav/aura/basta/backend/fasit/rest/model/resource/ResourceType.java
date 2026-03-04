package no.nav.aura.basta.backend.fasit.rest.model.resource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ResourceType {
    DataSource,
    MSSQLDataSource,
    DB2DataSource,
    LDAP,
    ADGROUP,
    BaseUrl,
    Credential,
    Certificate,
    Cics,
    RoleMapping,
    QueueManager,
    Datapower,
    Queue,
    DeploymentManager,
    ApplicationProperties,
    LoadBalancer,
    LoadBalancerConfig,
    Channel;

    /** Serialise back to the canonical enum name (e.g. "DataSource"). */
    @JsonValue
    public String toJson() {
        return this.name();
    }

    /**
     * Case-insensitive deserialisation: accepts "datasource", "DataSource",
     * "DATASOURCE", etc.
     */
    @JsonCreator
    public static ResourceType fromJson(String value) {
        for (ResourceType type : values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ResourceType: '" + value
                + "'. Valid values: " + java.util.Arrays.toString(values()));
    }
}
