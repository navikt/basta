package no.nav.aura.basta.backend.fasit.deprecated.envconfig.resources;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * Basepart of an url (Protocol, hostname and port) 
 * Like http://myserver:8080
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class BaseUrl extends AbstractPropertyResource {
}
