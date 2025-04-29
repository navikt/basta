package no.nav.aura.basta.backend.fasit.deprecated.envconfig.resources;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;


/**
 * Basepart of an url (Protocol, hostname and port) 
 * Like http://myserver:8080
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class BaseUrl extends AbstractPropertyResource {
}
