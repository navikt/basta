package no.nav.aura.basta.vmware.orchestrator.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class Fact {

    private String name;
    private String value;

    Fact() {
    }

    public Fact(FactType factType, String value) {
        this.setName(factType.name());
        this.setValue(value);

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
