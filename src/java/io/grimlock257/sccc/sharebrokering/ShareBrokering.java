package io.grimlock257.sccc.sharebrokering;

import io.github.grimlock257.stocks.StockPrice;
import io.github.grimlock257.stocks.StockPriceResponse;
import io.github.grimlock257.stocks.StockPriceSoap;
import io.grimlock257.sccc.jaxb.binding.SharePrice;
import io.grimlock257.sccc.jaxb.binding.Stock;
import io.grimlock257.sccc.jaxb.binding.Stocks;
import io.grimlock257.sccc.sharebrokering.manager.JAXBFileManager;
import io.grimlock257.sccc.sharebrokering.util.StringUtil;
import static io.grimlock257.sccc.sharebrokering.util.StringUtil.containsIgnoreCase;
import static io.grimlock257.sccc.sharebrokering.util.StringUtil.isNotNullOrEmpty;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
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

        return stocks
                .stream()
                .filter(stock -> isNotNullOrEmpty(stockName) ? containsIgnoreCase(stock.getStockName(), stockName) : true)
                .filter(stock -> isNotNullOrEmpty(stockSymbol) ? containsIgnoreCase(stock.getStockSymbol(), stockSymbol) : true)
                .filter(stock -> isNotNullOrEmpty(currency) ? stock.getPrice().getCurrency().equalsIgnoreCase(currency) : true)
                .filter(stock -> isNotNullOrEmpty(sharePriceFilter) && sharePrice >= 0 && sharePriceFilter.equalsIgnoreCase("lessOrEqual") ? (stock.getPrice().getPrice() <= sharePrice) : true)
                .filter(stock -> isNotNullOrEmpty(sharePriceFilter) && sharePrice >= 0 && sharePriceFilter.equalsIgnoreCase("equal") ? (stock.getPrice().getPrice() == sharePrice) : true)
                .filter(stock -> isNotNullOrEmpty(sharePriceFilter) && sharePrice >= 0 && sharePriceFilter.equalsIgnoreCase("greaterOrEqual") ? (stock.getPrice().getPrice() >= sharePrice) : true)
                .sorted(getStockSearchComparator(sortBy, order))
                .collect(Collectors.toList());
    }

    /**
     * Create a Comparator object for comparing Stocks for some given sort field and order method
     *
     * @param <T> A Stock object
     * @param <U> Some comparable property of the Stock object that will be used to sort
     * @param sortBy The column in which the results should be ordered by
     * @param order Whether to order the sortBy column ascending or descending
     * @return The Comparator object that can sort Stocks
     */
    private <T, U extends Comparable<U>> Comparator<T> getStockSearchComparator(String sortBy, String order) {
        // Store a function reference which takes a Stock and returns some Object
        Function<Stock, ? extends Object> sortFunction;

        // Determine the sort function based on the sortBy parameter
        switch (sortBy) {
            case "stockName":
                sortFunction = stock -> stock.getStockName();
                break;
            case "shareCurrency":
                sortFunction = stock -> stock.getPrice().getCurrency();
                break;
            case "sharePrice":
                sortFunction = stock -> stock.getPrice().getPrice();
                break;
            default:
                sortFunction = stock -> stock.getStockSymbol();
        }

        // Cast the sort function back to a fully generic sort function, as requried by the Comparator.comparing function
        Function<T, U> genericSortFunction = (Function<T, U>) sortFunction;

        // Return a comparator using the sort function determined above, reverse if the order is set to "desc"
        if (isNotNullOrEmpty(order) && order.equalsIgnoreCase("desc")) {
            return Comparator.comparing(genericSortFunction).reversed();
        } else {
            return Comparator.comparing(genericSortFunction);
        }
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
     * Remove the Stock with the supplied symbol if it exists
     *
     * @param stockSymbol The symbol for which to delete
     * @return Whether the stock deletion was successful or not
     */
    @WebMethod(operationName = "deleteShare")
    public boolean deleteShare(
            @WebParam(name = "stockSymbol") String stockSymbol
    ) {
        // Unmarshall stocks, find the stock with the supplied symbol, remove the stock and remarshall
        Stocks stocks = JAXBFileManager.getInstance().unmarshal();
        List<Stock> stocksList = stocks.getStocks();

        boolean wasRemoved = stocksList.removeIf(stock -> stock.getStockSymbol().equalsIgnoreCase(stockSymbol));

        if (wasRemoved) {
            return JAXBFileManager.getInstance().marshal(stocks);
        } else {
            return false;
        }
    }

    /**
     * Modify the Stock information for the supplied symbol if it exists
     *
     * @param stockName The new stock name
     * @param currentStockSymbol The current stock symbol, used to search record
     * @param newStockSymbol The new stock symbol
     * @param availableShares The new amount of shares
     * @return Whether the stock modification was successful or not
     */
    @WebMethod(operationName = "modifyShare")
    public boolean modifyShare(
            @WebParam(name = "stockName") String stockName,
            @WebParam(name = "currentStockSymbol") String currentStockSymbol,
            @WebParam(name = "newStockSymbol") String newStockSymbol,
            @WebParam(name = "availableShares") double availableShares
    ) {
        // Unmarshall stocks, find the stock with the supplied currentStockSymbol, edit the stock and remarshall
        Stocks stocks = JAXBFileManager.getInstance().unmarshal();
        List<Stock> stocksList = stocks.getStocks();

        boolean foundStock = false;
        boolean madeEdit = false;

        // Iterae over the list, if matching stock found, update information if provided
        for (Stock stock : stocksList) {
            if (stock.getStockSymbol().equalsIgnoreCase(currentStockSymbol)) {
                foundStock = true;

                if (StringUtil.isNotNullOrEmpty(stockName)) {
                    stock.setStockName(stockName);
                    madeEdit = true;
                }

                if (StringUtil.isNotNullOrEmpty(newStockSymbol)) {
                    stock.setStockSymbol(newStockSymbol);
                    madeEdit = true;
                }

                if (availableShares >= 0) {
                    stock.setAvailableShares(availableShares);
                    madeEdit = true;
                }

                break;
            }
        }

        if (foundStock && madeEdit) {
            return JAXBFileManager.getInstance().marshal(stocks);
        } else {
            return false;
        }
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
