
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
 *         &lt;element name="getWorkflowInputForWorkflowTokenIdReturn" type="{http://webservice.vso.dunes.ch}WorkflowInput"/>
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
    "getWorkflowInputForWorkflowTokenIdReturn"
})
@XmlRootElement(name = "getWorkflowInputForWorkflowTokenIdResponse")
public class GetWorkflowInputForWorkflowTokenIdResponse {

    @XmlElement(required = true)
    protected WorkflowInput getWorkflowInputForWorkflowTokenIdReturn;

    /**
     * Gets the value of the getWorkflowInputForWorkflowTokenIdReturn property.
     * 
     * @return
     *     possible object is
     *     {@link WorkflowInput }
     *     
     */
    public WorkflowInput getGetWorkflowInputForWorkflowTokenIdReturn() {
        return getWorkflowInputForWorkflowTokenIdReturn;
    }

    /**
     * Sets the value of the getWorkflowInputForWorkflowTokenIdReturn property.
     * 
     * @param value
     *     allowed object is
     *     {@link WorkflowInput }
     *     
     */
    public void setGetWorkflowInputForWorkflowTokenIdReturn(WorkflowInput value) {
        this.getWorkflowInputForWorkflowTokenIdReturn = value;
    }

}
