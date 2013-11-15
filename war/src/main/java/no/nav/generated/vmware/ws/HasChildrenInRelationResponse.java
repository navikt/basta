
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
 *         &lt;element name="hasChildrenInRelationReturn" type="{http://www.w3.org/2001/XMLSchema}int"/>
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
    "hasChildrenInRelationReturn"
})
@XmlRootElement(name = "hasChildrenInRelationResponse")
public class HasChildrenInRelationResponse {

    protected int hasChildrenInRelationReturn;

    /**
     * Gets the value of the hasChildrenInRelationReturn property.
     * 
     */
    public int getHasChildrenInRelationReturn() {
        return hasChildrenInRelationReturn;
    }

    /**
     * Sets the value of the hasChildrenInRelationReturn property.
     * 
     */
    public void setHasChildrenInRelationReturn(int value) {
        this.hasChildrenInRelationReturn = value;
    }

}
