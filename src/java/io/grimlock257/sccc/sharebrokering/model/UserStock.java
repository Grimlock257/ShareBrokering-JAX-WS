package io.grimlock257.sccc.sharebrokering.model;

import io.grimlock257.sccc.jaxb.binding.SharePrice;
import io.grimlock257.sccc.jaxb.binding.Stock;
import io.grimlock257.sccc.jaxb.binding.users.Share;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Represents a user owned stock (contains Stock information with added user specific information)
 *
 * @author Adam Watson
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class UserStock {

    private String stockName;
    private String stockSymbol;
    private double availableShares;
    private SharePrice price;

    private String userPurchaseCurrency;
    private double userQuantity;
    private double userPurchaseValue;

    public UserStock(Stock stock, Share share, String purchaseCurrency) {
        this.stockName = stock.getStockName();
        this.stockSymbol = stock.getStockSymbol();
        this.availableShares = stock.getAvailableShares();
        this.price = stock.getPrice();

        this.userPurchaseCurrency = purchaseCurrency;
        this.userQuantity = share.getQuantity();
        this.userPurchaseValue = share.getPurchaseValue();
    }

    public String getStockName() {
        return stockName;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public double getAvailableShares() {
        return availableShares;
    }

    public SharePrice getPrice() {
        return price;
    }

    public String getUserPurchaseCurrency() {
        return userPurchaseCurrency;
    }

    public double getUserQuantity() {
        return userQuantity;
    }

    public double getUserPurchaseValue() {
        return userPurchaseValue;
    }
}
