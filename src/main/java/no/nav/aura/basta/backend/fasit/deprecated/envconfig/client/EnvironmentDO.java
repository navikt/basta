package no.nav.aura.basta.backend.fasit.deprecated.envconfig.client;

import java.net.URI;

import jakarta.ws.rs.core.UriBuilder;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "environment")
@XmlAccessorType(XmlAccessType.FIELD)
public class EnvironmentDO {
    @XmlAttribute
    private URI ref;
    private String name;
    private String envClass;
    private URI applicationsRef;

    public EnvironmentDO() {
    }

    public EnvironmentDO(String name, String envClass, UriBuilder uriBuilder) {
        this.name = name;
        this.envClass = envClass;
        ref = uriBuilder.clone().path("conf/environments/{env}").build(name);
        applicationsRef = uriBuilder.clone().path("conf/environments/{env}/applications").build(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnvClass() {
        return envClass;
    }

    public void setEnvClass(String envClass) {
        this.envClass = envClass;
    }

    public URI getRef() {
        return ref;
    }

    public URI getApplicationsRef() {
        return applicationsRef;
    }
}
