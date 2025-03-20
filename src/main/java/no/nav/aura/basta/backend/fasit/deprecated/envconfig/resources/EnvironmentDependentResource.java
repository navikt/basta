package no.nav.aura.basta.backend.fasit.deprecated.envconfig.resources;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * Resource depending on fasit properties
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class EnvironmentDependentResource extends Resource {

    @XmlAttribute(required = true)
    private String alias;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

}
