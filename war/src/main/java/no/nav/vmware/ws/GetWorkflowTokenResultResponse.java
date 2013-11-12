
package no.nav.vmware.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element name="getWorkflowTokenResultReturn" type="{http://webservice.vso.dunes.ch}WorkflowTokenAttribute" maxOccurs="unbounded" minOccurs="0"/>
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
    "getWorkflowTokenResultReturn"
})
@XmlRootElement(name = "getWorkflowTokenResultResponse")
public class GetWorkflowTokenResultResponse {

    protected List<WorkflowTokenAttribute> getWorkflowTokenResultReturn;

    /**
     * Gets the value of the getWorkflowTokenResultReturn property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the getWorkflowTokenResultReturn property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGetWorkflowTokenResultReturn().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WorkflowTokenAttribute }
     * 
     * 
     */
    public List<WorkflowTokenAttribute> getGetWorkflowTokenResultReturn() {
        if (getWorkflowTokenResultReturn == null) {
            getWorkflowTokenResultReturn = new ArrayList<WorkflowTokenAttribute>();
        }
        return this.getWorkflowTokenResultReturn;
    }

}
