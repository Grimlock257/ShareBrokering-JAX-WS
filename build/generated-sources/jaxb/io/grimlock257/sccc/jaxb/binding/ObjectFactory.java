//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.11.10 at 11:45:41 PM GMT 
//


package io.grimlock257.sccc.jaxb.binding;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the io.grimlock257.sccc.jaxb.binding package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: io.grimlock257.sccc.jaxb.binding
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Stocks }
     * 
     */
    public Stocks createStocks() {
        return new Stocks();
    }

    /**
     * Create an instance of {@link Stock }
     * 
     */
    public Stock createStock() {
        return new Stock();
    }

    /**
     * Create an instance of {@link SharePrice }
     * 
     */
    public SharePrice createSharePrice() {
        return new SharePrice();
    }

}
