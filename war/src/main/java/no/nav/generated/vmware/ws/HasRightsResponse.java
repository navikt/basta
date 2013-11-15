
package no.nav.generated.vmware.ws;

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
 *         &lt;element name="hasRightsReturn" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
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
    "hasRightsReturn"
})
@XmlRootElement(name = "hasRightsResponse")
public class HasRightsResponse {

    protected boolean hasRightsReturn;

    /**
     * Gets the value of the hasRightsReturn property.
     * 
     */
    public boolean isHasRightsReturn() {
        return hasRightsReturn;
    }

    /**
     * Sets the value of the hasRightsReturn property.
     * 
     */
    public void setHasRightsReturn(boolean value) {
        this.hasRightsReturn = value;
    }

}
