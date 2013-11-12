
package no.nav.vmware.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for QueryResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QueryResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="totalCount" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="elements" type="{http://webservice.vso.dunes.ch}ArrayOfFinderResult"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryResult", propOrder = {
    "totalCount",
    "elements"
})
public class QueryResult {

    protected long totalCount;
    @XmlElement(required = true, nillable = true)
    protected ArrayOfFinderResult elements;

    /**
     * Gets the value of the totalCount property.
     * 
     */
    public long getTotalCount() {
        return totalCount;
    }

    /**
     * Sets the value of the totalCount property.
     * 
     */
    public void setTotalCount(long value) {
        this.totalCount = value;
    }

    /**
     * Gets the value of the elements property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfFinderResult }
     *     
     */
    public ArrayOfFinderResult getElements() {
        return elements;
    }

    /**
     * Sets the value of the elements property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfFinderResult }
     *     
     */
    public void setElements(ArrayOfFinderResult value) {
        this.elements = value;
    }

}
