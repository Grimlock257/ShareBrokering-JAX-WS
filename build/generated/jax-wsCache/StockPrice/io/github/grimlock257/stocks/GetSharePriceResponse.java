
package io.github.grimlock257.stocks;

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
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="GetSharePriceResult" type="{http://grimlock257.github.io/Stocks}StockPriceResponse" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "getSharePriceResult"
})
@XmlRootElement(name = "GetSharePriceResponse")
public class GetSharePriceResponse {

    @XmlElement(name = "GetSharePriceResult")
    protected StockPriceResponse getSharePriceResult;

    /**
     * Gets the value of the getSharePriceResult property.
     * 
     * @return
     *     possible object is
     *     {@link StockPriceResponse }
     *     
     */
    public StockPriceResponse getGetSharePriceResult() {
        return getSharePriceResult;
    }

    /**
     * Sets the value of the getSharePriceResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link StockPriceResponse }
     *     
     */
    public void setGetSharePriceResult(StockPriceResponse value) {
        this.getSharePriceResult = value;
    }

}
