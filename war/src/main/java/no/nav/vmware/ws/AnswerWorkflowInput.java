
package no.nav.vmware.ws;

import java.util.ArrayList;
import java.util.List;
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
 *         &lt;element name="workflowTokenId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="answerInputs" type="{http://webservice.vso.dunes.ch}WorkflowTokenAttribute" maxOccurs="unbounded"/>
 *         &lt;element name="username" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="password" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "workflowTokenId",
    "answerInputs",
    "username",
    "password"
})
@XmlRootElement(name = "answerWorkflowInput")
public class AnswerWorkflowInput {

    @XmlElement(required = true)
    protected String workflowTokenId;
    @XmlElement(required = true)
    protected List<WorkflowTokenAttribute> answerInputs;
    @XmlElement(required = true)
    protected String username;
    @XmlElement(required = true)
    protected String password;

    /**
     * Gets the value of the workflowTokenId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWorkflowTokenId() {
        return workflowTokenId;
    }

    /**
     * Sets the value of the workflowTokenId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWorkflowTokenId(String value) {
        this.workflowTokenId = value;
    }

    /**
     * Gets the value of the answerInputs property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the answerInputs property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAnswerInputs().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WorkflowTokenAttribute }
     * 
     * 
     */
    public List<WorkflowTokenAttribute> getAnswerInputs() {
        if (answerInputs == null) {
            answerInputs = new ArrayList<WorkflowTokenAttribute>();
        }
        return this.answerInputs;
    }

    /**
     * Gets the value of the username property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the value of the username property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsername(String value) {
        this.username = value;
    }

    /**
     * Gets the value of the password property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the value of the password property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassword(String value) {
        this.password = value;
    }

}
