
package no.nav.vmware.ws;

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
 *         &lt;element name="echoWorkflowReturn" type="{http://webservice.vso.dunes.ch}Workflow"/>
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
    "echoWorkflowReturn"
})
@XmlRootElement(name = "echoWorkflowResponse")
public class EchoWorkflowResponse {

    @XmlElement(required = true)
    protected Workflow echoWorkflowReturn;

    /**
     * Gets the value of the echoWorkflowReturn property.
     * 
     * @return
     *     possible object is
     *     {@link Workflow }
     *     
     */
    public Workflow getEchoWorkflowReturn() {
        return echoWorkflowReturn;
    }

    /**
     * Sets the value of the echoWorkflowReturn property.
     * 
     * @param value
     *     allowed object is
     *     {@link Workflow }
     *     
     */
    public void setEchoWorkflowReturn(Workflow value) {
        this.echoWorkflowReturn = value;
    }

}
