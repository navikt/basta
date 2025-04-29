package no.nav.aura.basta.backend.fasit.deprecated.envconfig.resources;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlSeeAlso;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({Datasource.class, XmlDatasource.class, Channel.class, Credential.class, ApplicationCertificate.class, QueueManager.class,
        Ldap.class, })
public abstract class Resource {

}
