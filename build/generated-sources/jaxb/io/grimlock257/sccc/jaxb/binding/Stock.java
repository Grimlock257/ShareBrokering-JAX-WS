//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.11.09 at 08:10:24 PM GMT 
//


package io.grimlock257.sccc.jaxb.binding;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Stock complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Stock">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="StockName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="StockSymbol" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="AvailableShares" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Price" type="{http://grimlock257.github.io/Stocks}SharePrice"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Stock", propOrder = {
    "stockName",
    "stockSymbol",
    "availableShares",
    "price"
})
public class Stock {

    @XmlElement(name = "StockName", required = true)
    protected String stockName;
    @XmlElement(name = "StockSymbol", required = true)
    protected String stockSymbol;
    @XmlElement(name = "AvailableShares")
    protected int availableShares;
    @XmlElement(name = "Price", required = true)
    protected SharePrice price;

    /**
     * Gets the value of the stockName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStockName() {
        return stockName;
    }

    /**
     * Sets the value of the stockName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStockName(String value) {
        this.stockName = value;
    }

    /**
     * Gets the value of the stockSymbol property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStockSymbol() {
        return stockSymbol;
    }

    /**
     * Sets the value of the stockSymbol property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStockSymbol(String value) {
        this.stockSymbol = value;
    }

    /**
     * Gets the value of the availableShares property.
     * 
     */
    public int getAvailableShares() {
        return availableShares;
    }

    /**
     * Sets the value of the availableShares property.
     * 
     */
    public void setAvailableShares(int value) {
        this.availableShares = value;
    }

    /**
     * Gets the value of the price property.
     * 
     * @return
     *     possible object is
     *     {@link SharePrice }
     *     
     */
    public SharePrice getPrice() {
        return price;
    }

    /**
     * Sets the value of the price property.
     * 
     * @param value
     *     allowed object is
     *     {@link SharePrice }
     *     
     */
    public void setPrice(SharePrice value) {
        this.price = value;
    }

}
