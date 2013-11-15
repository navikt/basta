
package no.nav.generated.vmware.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="getWorkflowForIdReturn" type="{http://webservice.vso.dunes.ch}Workflow"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "getWorkflowForIdReturn"
})
@XmlRootElement(name = "getWorkflowForIdResponse")
public class GetWorkflowForIdResponse {

    @XmlElement(required = true)
    protected Workflow getWorkflowForIdReturn;

    /**
     * Gets the value of the getWorkflowForIdReturn property.
     * 
     * @return
     *     possible object is
     *     {@link Workflow }
     *     
     */
    public Workflow getGetWorkflowForIdReturn() {
        return getWorkflowForIdReturn;
    }

    /**
     * Sets the value of the getWorkflowForIdReturn property.
     * 
     * @param value
     *     allowed object is
     *     {@link Workflow }
     *     
     */
    public void setGetWorkflowForIdReturn(Workflow value) {
        this.getWorkflowForIdReturn = value;
    }

}
