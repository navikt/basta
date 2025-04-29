package no.nav.aura.basta.backend.fasit.deprecated.envconfig.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElementRef;

import no.nav.aura.basta.backend.fasit.deprecated.envconfig.jaxb.JaxbPropertyHelper;
import no.nav.aura.basta.backend.fasit.deprecated.envconfig.jaxb.JaxbPropertySet;
import no.nav.aura.basta.backend.fasit.deprecated.envconfig.jaxb.ParentConfigObject;


@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractJndiCapableResource extends AbstractPropertyResource {

    @XmlAttribute(required = false)
    private String jndi;

    @XmlElementRef
    protected List<JaxbPropertySet> customProperties = new ArrayList<>();

    public String getJndi() {
        return jndi;
    }

    public void setJndi(String jndi) {
        this.jndi = jndi;
    }

    public void setCustomProperties(List<JaxbPropertySet> customProperties) {
        this.customProperties = customProperties;
    }

    public Map<String, String> getJ2EEResourceProperties() {
        return getCustomProperties(ParentConfigObject.J2EEResourceProperty);
    }

    public Map<String, String> getCustomProperties(ParentConfigObject parentObject) {
        return JaxbPropertyHelper.getCustomProperties(parentObject, customProperties);
    }

    public Map<String, String> getCustomProperties() {
        return JaxbPropertyHelper.getCustomProperties(customProperties);
    }

}
