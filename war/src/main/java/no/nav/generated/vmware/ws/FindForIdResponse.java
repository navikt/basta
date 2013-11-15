
package no.nav.generated.vmware.ws;

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
 *         &lt;element name="findForIdReturn" type="{http://webservice.vso.dunes.ch}FinderResult"/>
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
    "findForIdReturn"
})
@XmlRootElement(name = "findForIdResponse")
public class FindForIdResponse {

    @XmlElement(required = true)
    protected FinderResult findForIdReturn;

    /**
     * Gets the value of the findForIdReturn property.
     * 
     * @return
     *     possible object is
     *     {@link FinderResult }
     *     
     */
    public FinderResult getFindForIdReturn() {
        return findForIdReturn;
    }

    /**
     * Sets the value of the findForIdReturn property.
     * 
     * @param value
     *     allowed object is
     *     {@link FinderResult }
     *     
     */
    public void setFindForIdReturn(FinderResult value) {
        this.findForIdReturn = value;
    }

}
