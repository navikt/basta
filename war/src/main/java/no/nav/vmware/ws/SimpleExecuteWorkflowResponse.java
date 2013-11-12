
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
 *         &lt;element name="simpleExecuteWorkflowReturn" type="{http://webservice.vso.dunes.ch}WorkflowToken"/>
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
    "simpleExecuteWorkflowReturn"
})
@XmlRootElement(name = "simpleExecuteWorkflowResponse")
public class SimpleExecuteWorkflowResponse {

    @XmlElement(required = true)
    protected WorkflowToken simpleExecuteWorkflowReturn;

    /**
     * Gets the value of the simpleExecuteWorkflowReturn property.
     * 
     * @return
     *     possible object is
     *     {@link WorkflowToken }
     *     
     */
    public WorkflowToken getSimpleExecuteWorkflowReturn() {
        return simpleExecuteWorkflowReturn;
    }

    /**
     * Sets the value of the simpleExecuteWorkflowReturn property.
     * 
     * @param value
     *     allowed object is
     *     {@link WorkflowToken }
     *     
     */
    public void setSimpleExecuteWorkflowReturn(WorkflowToken value) {
        this.simpleExecuteWorkflowReturn = value;
    }

}
