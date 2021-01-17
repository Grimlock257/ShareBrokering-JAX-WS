package io.grimlock257.sccc.sharebrokering.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Represents a funds response model to return to the client
 *
 * @author Adam Watson
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class FundsResponse {

    private double availableFunds;
    private String currency;

    public FundsResponse(double availableFunds, String currency) {
        this.availableFunds = availableFunds;
        this.currency = currency;
    }

    public double getAvailableFunds() {
        return availableFunds;
    }

    public String getCurrency() {
        return currency;
    }
}
