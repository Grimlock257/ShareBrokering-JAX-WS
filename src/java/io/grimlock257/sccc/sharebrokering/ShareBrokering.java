package io.grimlock257.sccc.sharebrokering;

import io.github.grimlock257.stocks.StockPrice;
import io.github.grimlock257.stocks.StockPriceResponse;
import io.github.grimlock257.stocks.StockPriceSoap;
import io.grimlock257.sccc.jaxb.binding.SharePrice;
import io.grimlock257.sccc.jaxb.binding.Stock;
import io.grimlock257.sccc.jaxb.binding.Stocks;
import io.grimlock257.sccc.sharebrokering.manager.JAXBFileManager;
import io.grimlock257.sccc.sharebrokering.util.StringUtil;
import java.util.ArrayList;
import java.util.Comparator;
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
     * Unmarshalls the Stocks XML file and looks for a Stock with the
     * provided stock symbol
     *
     * @param companySymbol The symbol to search for
     * @return The Stock object for the given companySymbol
     */
    @WebMethod(operationName = "getStockBySymbol")
    public Stock getStockBySymbol(
            @WebParam(name = "companySymbol") String companySymbol
    ) {
        Stocks stocks = JAXBFileManager.getInstance().unmarshal();

        for (Stock stock : stocks.getStocks()) {
            if (StringUtil.isNotNullOrEmpty(companySymbol) && stock.getStockSymbol().equalsIgnoreCase(companySymbol)) {
                return stock;
            }
        }

        return null;
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

    /**
     * Unmarshalls the Stocks XML file, then iterates over each stock, checking
     * it against a set of criteria. The checks within check if the stock has
     * <b>failed</b> against a criteria. If the end of the iteration is reached,
     * the stock in question is added to a list to be returned to the caller.
     * The results are finally sorted by the column and order specified.
     *
     * @param stockName See if value is contained in any stock names
     * @param stockSymbol See if value is contained in any stock symbols
     * @param currency See is value matches any currencies
     * @param sharePriceFilter How to interpret the share price field
     * @param sharePrice The price by which to search based on
     * @param sortBy The column in which the results should be ordered by
     * @param order Whether to order the sortBy column ascending or descending
     * @return A list of filtered Stock objects based on the provided criteria
     */
    @WebMethod(operationName = "searchShares")
    public List<Stock> searchShares(
            @WebParam(name = "stockName") String stockName,
            @WebParam(name = "stockSymbol") String stockSymbol,
            @WebParam(name = "currency") String currency,
            @WebParam(name = "sharePriceFilter") String sharePriceFilter,
            @WebParam(name = "sharePrice") double sharePrice,
            @WebParam(name = "sortBy") String sortBy,
            @WebParam(name = "order") String order
    ) {

        List<Stock> stocks = JAXBFileManager.getInstance().unmarshal().getStocks();
        List<Stock> filteredStocks = new ArrayList<>();

        for (Stock stock : stocks) {
            if (StringUtil.isNotNullOrEmpty(stockName) && !StringUtil.containsIgnoreCase(stock.getStockName(), stockName)) {
                continue;
            }

            if (StringUtil.isNotNullOrEmpty(stockSymbol) && !StringUtil.containsIgnoreCase(stock.getStockSymbol(), stockSymbol)) {
                continue;
            }

            if (StringUtil.isNotNullOrEmpty(currency) && !stock.getPrice().getCurrency().equalsIgnoreCase(currency)) {
                continue;
            }

            if (StringUtil.isNotNullOrEmpty(sharePriceFilter) && sharePrice >= 0) {
                switch (sharePriceFilter) {
                    case "lessOrEqual":
                        if (stock.getPrice().getPrice() > sharePrice) {
                            continue;
                        }

                        break;
                    case "equal":
                        if (stock.getPrice().getPrice() != sharePrice) {
                            continue;
                        }

                        break;
                    case "greaterOrEqual":
                        if (stock.getPrice().getPrice() < sharePrice) {
                            continue;
                        }

                        break;
                }
            }

            filteredStocks.add(stock);
        }

        if (StringUtil.isNotNullOrEmpty(sortBy)) {
            switch (sortBy) {
                case "stockName":
                    if (StringUtil.isNotNullOrEmpty(order) && order.equalsIgnoreCase("desc")) {
                        filteredStocks.sort(Comparator.comparing(Stock::getStockName).reversed());
                    } else {
                        filteredStocks.sort(Comparator.comparing(Stock::getStockName));
                    }
                    break;
                case "stockSymbol":
                    if (StringUtil.isNotNullOrEmpty(order) && order.equalsIgnoreCase("desc")) {
                        filteredStocks.sort(Comparator.comparing(Stock::getStockSymbol).reversed());
                    } else {
                        filteredStocks.sort(Comparator.comparing(Stock::getStockSymbol));
                    }
                    break;
                case "shareCurrency":
                    if (StringUtil.isNotNullOrEmpty(order) && order.equalsIgnoreCase("desc")) {
                        filteredStocks.sort(Comparator.comparing((Stock stock) -> stock.getPrice().getCurrency()).reversed());
                    } else {
                        filteredStocks.sort(Comparator.comparing(stock -> stock.getPrice().getCurrency()));
                    }
                    break;
                case "sharePrice":
                    if (StringUtil.isNotNullOrEmpty(order) && order.equalsIgnoreCase("desc")) {
                        filteredStocks.sort(Comparator.comparing((Stock stock) -> stock.getPrice().getPrice()).reversed());
                    } else {
                        filteredStocks.sort(Comparator.comparing(stock -> stock.getPrice().getPrice()));
                    }
                    break;
            }
        }

        return filteredStocks;
    }

    /**
     * Creates a new Stock object based on the provided values, then unmarshalls the
     * Stocks XML file, retrieves the list and adds the new Stock object before
     * marshalling the file again.
     *
     * @param stockName The company name of the new stock
     * @param stockSymbol The symbol for the new stock
     * @param availableShares The amount of available shares for the new stock
     * @return Whether the stock addition was successful or not
     */
    @WebMethod(operationName = "addShare")
    public boolean addShare(
            @WebParam(name = "stockName") String stockName,
            @WebParam(name = "stockSymbol") String stockSymbol,
            @WebParam(name = "availableShares") double availableShares
    ) {
        // Create new Stock object based on supplied information
        StockPriceResponse stockPrice = getSharePrice(stockSymbol);

        SharePrice sharePrice = new SharePrice();
        sharePrice.setCurrency(stockPrice.getStockCurrency());
        sharePrice.setPrice(stockPrice.getStockPrice());
        sharePrice.setUpdated(stockPrice.getStockPriceTime());

        Stock stock = new Stock();
        stock.setAvailableShares(availableShares);
        stock.setStockName(stockName);
        stock.setStockSymbol(stockSymbol);
        stock.setPrice(sharePrice);

        // Unmarshall stocks, add the new Stock and remarshall
        Stocks stocks = JAXBFileManager.getInstance().unmarshal();
        List<Stock> stocksList = stocks.getStocks();
        stocksList.add(stock);

        return JAXBFileManager.getInstance().marshal(stocks);
    }

    /**
     * Retrieve the stock price information from the remote web service for the provided stock symbol
     *
     * @param symbol The stock symbol for which to retrieve stock price information for
     * @return The share price, currency and updated time contained within a StockPriceResponse object
     */
    private StockPriceResponse getSharePrice(String symbol) {
        StockPrice service = new StockPrice();
        StockPriceSoap port = service.getStockPriceSoap();

        return port.getSharePrice(symbol);
    }
}
