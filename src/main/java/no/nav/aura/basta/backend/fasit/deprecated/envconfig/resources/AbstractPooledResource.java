package no.nav.aura.basta.backend.fasit.deprecated.envconfig.resources;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;


public class AbstractPooledResource extends AbstractJndiCapableResource {

    @XmlAttribute(required = false)
    private boolean xaEnabled = false;

    @XmlElement(namespace = Namespaces.DEFAULT)
    private ConnectionPool pool = new ConnectionPool();

    public boolean isXaEnabled() {
        return xaEnabled;
    }

    public void setXaEnabled(boolean xaEnabled) {
        this.xaEnabled = xaEnabled;
    }

    public ConnectionPool getPool() {
        return pool;
    }

    public void setPool(ConnectionPool pool) {
        this.pool = pool;
    }
}
