
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
 *         &lt;element name="getWorkflowTokenForIdReturn" type="{http://webservice.vso.dunes.ch}WorkflowToken"/>
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
    "getWorkflowTokenForIdReturn"
})
@XmlRootElement(name = "getWorkflowTokenForIdResponse")
public class GetWorkflowTokenForIdResponse {

    @XmlElement(required = true)
    protected WorkflowToken getWorkflowTokenForIdReturn;

    /**
     * Gets the value of the getWorkflowTokenForIdReturn property.
     * 
     * @return
     *     possible object is
     *     {@link WorkflowToken }
     *     
     */
    public WorkflowToken getGetWorkflowTokenForIdReturn() {
        return getWorkflowTokenForIdReturn;
    }

    /**
     * Sets the value of the getWorkflowTokenForIdReturn property.
     * 
     * @param value
     *     allowed object is
     *     {@link WorkflowToken }
     *     
     */
    public void setGetWorkflowTokenForIdReturn(WorkflowToken value) {
        this.getWorkflowTokenForIdReturn = value;
    }

}
