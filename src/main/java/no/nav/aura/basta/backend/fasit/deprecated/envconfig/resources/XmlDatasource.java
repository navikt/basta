package no.nav.aura.basta.backend.fasit.deprecated.envconfig.resources;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlDatasource extends Datasource {

    public XmlDatasource() {
    }

    public XmlDatasource(String alias, DBType type, String jndi) {
        super(alias, type, jndi);
    }
}
