
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
 *         &lt;element name="findRelationReturn" type="{http://webservice.vso.dunes.ch}FinderResult" maxOccurs="unbounded" minOccurs="0"/>
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
    "findRelationReturn"
})
@XmlRootElement(name = "findRelationResponse")
public class FindRelationResponse {

    protected List<FinderResult> findRelationReturn;

    /**
     * Gets the value of the findRelationReturn property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the findRelationReturn property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFindRelationReturn().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FinderResult }
     * 
     * 
     */
    public List<FinderResult> getFindRelationReturn() {
        if (findRelationReturn == null) {
            findRelationReturn = new ArrayList<FinderResult>();
        }
        return this.findRelationReturn;
    }

}
