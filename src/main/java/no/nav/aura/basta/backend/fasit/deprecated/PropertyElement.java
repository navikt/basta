package no.nav.aura.basta.backend.fasit.deprecated;

import static no.nav.aura.basta.backend.fasit.deprecated.PropertyElement.Type.STRING;

import java.net.URI;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "property")
@XmlAccessorType(XmlAccessType.FIELD)
@Deprecated
public class PropertyElement {

    public enum Type {
        STRING, SECRET, FILE
    }

    @XmlAttribute
    private String name;
    @XmlAttribute
    private Type type = STRING;
    private String value;
    private URI ref;

    public PropertyElement() {
    }

    public PropertyElement(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public PropertyElement(String name, URI ref, Type type) {
        this.type = type;
        this.name = name;
        this.ref = ref;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public URI getRef() {
        return ref;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "PropertyElement [name=" + name + ", type=" + type + ", value=" + value + ", ref=" + ref + "]";
    }

}
