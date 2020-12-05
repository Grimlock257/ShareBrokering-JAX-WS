
package io.github.grimlock257.stocks;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for StockPriceResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StockPriceResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="StockPrice" type="{http://www.w3.org/2001/XMLSchema}double"/&gt;
 *         &lt;element name="StockCurrency" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="StockPriceTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StockPriceResponse", propOrder = {
    "stockPrice",
    "stockCurrency",
    "stockPriceTime"
})
public class StockPriceResponse {

    @XmlElement(name = "StockPrice")
    protected double stockPrice;
    @XmlElement(name = "StockCurrency")
    protected String stockCurrency;
    @XmlElement(name = "StockPriceTime", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar stockPriceTime;

    /**
     * Gets the value of the stockPrice property.
     * 
     */
    public double getStockPrice() {
        return stockPrice;
    }

    /**
     * Sets the value of the stockPrice property.
     * 
     */
    public void setStockPrice(double value) {
        this.stockPrice = value;
    }

    /**
     * Gets the value of the stockCurrency property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStockCurrency() {
        return stockCurrency;
    }

    /**
     * Sets the value of the stockCurrency property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStockCurrency(String value) {
        this.stockCurrency = value;
    }

    /**
     * Gets the value of the stockPriceTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getStockPriceTime() {
        return stockPriceTime;
    }

    /**
     * Sets the value of the stockPriceTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setStockPriceTime(XMLGregorianCalendar value) {
        this.stockPriceTime = value;
    }

}
