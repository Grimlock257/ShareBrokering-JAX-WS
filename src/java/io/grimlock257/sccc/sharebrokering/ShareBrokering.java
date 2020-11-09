package io.grimlock257.sccc.sharebrokering;

import io.grimlock257.sccc.jaxb.binding.Stock;
import io.grimlock257.sccc.jaxb.binding.Stocks;
import io.grimlock257.sccc.sharebrokering.manager.JAXBFileManager;
import java.util.List;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * @author Adam Watson
 */
@WebService(serviceName = "ShareBrokering")
@Stateless()
public class ShareBrokering {

    /**
     * Unmarshalls the Stocks XML file and returns the list from within
     *
     * @return A List object containing Stock objects
     */
    @WebMethod(operationName = "getAllStocks")
    public List<Stock> getAllStocks() {
        return JAXBFileManager.getInstance().unmarshal().getStocks();
    }

    /**
     * Unmarshalls the Stocks XML file, then iterates over each stock, checking
     * the symbol of that stock compared to the provided stock symbol, and that
     * the amount of available shares is greater or equal to the desired
     * quantity to purchase. If both these criteria are met, reduce the
     * available shares by the purchase quantity amount, and attempt to marshal
     * the changed XML.
     *
     * @param companySymbol The symbol of the company to purchase shares from
     * @param quantity The amount of shares to purchase
     * @return Whether the operation was successful
     */
    @WebMethod(operationName = "purchaseShare")
    public Boolean purchaseShare(
            @WebParam(name = "companySymbol") String companySymbol,
            @WebParam(name = "quantity") double quantity
    ) {

        Stocks stocks = JAXBFileManager.getInstance().unmarshal();

        for (Stock stock : stocks.getStocks()) {
            if (stock.getStockSymbol().equalsIgnoreCase(companySymbol) && stock.getAvailableShares() >= quantity) {
                stock.setAvailableShares(stock.getAvailableShares() - quantity);

                return JAXBFileManager.getInstance().marshal(stocks);
            }
        }

        return false;
    }

    /**
     * Unmarshalls the Stocks XML file, then iterates over each stock, checking
     * the symbol of that stock compared to the provided stock symbol. If this
     * criteria is met, then increase the available shares by the sell quantity
     * amount, and attempt to marshal the changed XML
     *
     * @param companySymbol The symbol of the company to sell shares to
     * @param quantity The amount of shares to sell
     * @return Whether the operation was successful
     */
    @WebMethod(operationName = "sellShare")
    public Boolean sellShare(
            @WebParam(name = "companySymbol") String companySymbol,
            @WebParam(name = "quantity") double quantity
    ) {

        Stocks stocks = JAXBFileManager.getInstance().unmarshal();

        for (Stock stock : stocks.getStocks()) {
            if (stock.getStockSymbol().equalsIgnoreCase(companySymbol)) {
                stock.setAvailableShares(stock.getAvailableShares() + quantity);

                return JAXBFileManager.getInstance().marshal(stocks);
            }
        }

        return false;
    }
}
