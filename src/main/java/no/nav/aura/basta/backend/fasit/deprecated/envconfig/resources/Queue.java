package no.nav.aura.basta.backend.fasit.deprecated.envconfig.resources;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import no.nav.aura.basta.backend.fasit.deprecated.envconfig.resources.Namespaces;

@XmlRootElement(name = "queue")
@XmlAccessorType(XmlAccessType.FIELD)
public class Queue extends JmsDestination {

    @XmlElement(namespace = Namespaces.DEFAULT)
    private ListenerPort listenerPort;

    @XmlElement(namespace = Namespaces.DEFAULT)
    private ActivationSpec activationSpec;

    public Queue() {
    }

    public Queue(String alias) {
        setAlias(alias);
    }

    public ListenerPort getListenerPort() {
        return listenerPort;
    }

    public ActivationSpec getActivationSpec() {
        return activationSpec;
    }

}
