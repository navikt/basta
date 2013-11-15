
package no.nav.generated.vmware.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Workflow complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Workflow">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="inParameters" type="{http://webservice.vso.dunes.ch}ArrayOfWorkflowParameter"/>
 *         &lt;element name="outParameters" type="{http://webservice.vso.dunes.ch}ArrayOfWorkflowParameter"/>
 *         &lt;element name="attributes" type="{http://webservice.vso.dunes.ch}ArrayOfWorkflowParameter"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Workflow", propOrder = {
    "id",
    "name",
    "description",
    "inParameters",
    "outParameters",
    "attributes"
})
public class Workflow {

    @XmlElement(required = true, nillable = true)
    protected String id;
    @XmlElement(required = true, nillable = true)
    protected String name;
    @XmlElement(required = true, nillable = true)
    protected String description;
    @XmlElement(required = true, nillable = true)
    protected ArrayOfWorkflowParameter inParameters;
    @XmlElement(required = true, nillable = true)
    protected ArrayOfWorkflowParameter outParameters;
    @XmlElement(required = true, nillable = true)
    protected ArrayOfWorkflowParameter attributes;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the inParameters property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfWorkflowParameter }
     *     
     */
    public ArrayOfWorkflowParameter getInParameters() {
        return inParameters;
    }

    /**
     * Sets the value of the inParameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfWorkflowParameter }
     *     
     */
    public void setInParameters(ArrayOfWorkflowParameter value) {
        this.inParameters = value;
    }

    /**
     * Gets the value of the outParameters property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfWorkflowParameter }
     *     
     */
    public ArrayOfWorkflowParameter getOutParameters() {
        return outParameters;
    }

    /**
     * Sets the value of the outParameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfWorkflowParameter }
     *     
     */
    public void setOutParameters(ArrayOfWorkflowParameter value) {
        this.outParameters = value;
    }

    /**
     * Gets the value of the attributes property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfWorkflowParameter }
     *     
     */
    public ArrayOfWorkflowParameter getAttributes() {
        return attributes;
    }

    /**
     * Sets the value of the attributes property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfWorkflowParameter }
     *     
     */
    public void setAttributes(ArrayOfWorkflowParameter value) {
        this.attributes = value;
    }

}
