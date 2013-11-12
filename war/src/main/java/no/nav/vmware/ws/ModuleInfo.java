
package no.nav.vmware.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ModuleInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ModuleInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="moduleName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="moduleVersion" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="moduleDescription" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="moduleDisplayName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ModuleInfo", propOrder = {
    "moduleName",
    "moduleVersion",
    "moduleDescription",
    "moduleDisplayName"
})
public class ModuleInfo {

    @XmlElement(required = true, nillable = true)
    protected String moduleName;
    @XmlElement(required = true, nillable = true)
    protected String moduleVersion;
    @XmlElement(required = true, nillable = true)
    protected String moduleDescription;
    @XmlElement(required = true, nillable = true)
    protected String moduleDisplayName;

    /**
     * Gets the value of the moduleName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * Sets the value of the moduleName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModuleName(String value) {
        this.moduleName = value;
    }

    /**
     * Gets the value of the moduleVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModuleVersion() {
        return moduleVersion;
    }

    /**
     * Sets the value of the moduleVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModuleVersion(String value) {
        this.moduleVersion = value;
    }

    /**
     * Gets the value of the moduleDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModuleDescription() {
        return moduleDescription;
    }

    /**
     * Sets the value of the moduleDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModuleDescription(String value) {
        this.moduleDescription = value;
    }

    /**
     * Gets the value of the moduleDisplayName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModuleDisplayName() {
        return moduleDisplayName;
    }

    /**
     * Sets the value of the moduleDisplayName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModuleDisplayName(String value) {
        this.moduleDisplayName = value;
    }

}
