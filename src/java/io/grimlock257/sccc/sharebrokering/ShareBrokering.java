package io.grimlock257.sccc.sharebrokering;

import io.github.grimlock257.stocks.StockPrice;
import io.github.grimlock257.stocks.StockPriceResponse;
import io.github.grimlock257.stocks.StockPriceSoap;
import io.grimlock257.sccc.jaxb.binding.SharePrice;
import io.grimlock257.sccc.jaxb.binding.Stock;
import io.grimlock257.sccc.jaxb.binding.Stocks;
import io.grimlock257.sccc.jaxb.binding.users.Role;
import io.grimlock257.sccc.jaxb.binding.users.Share;
import io.grimlock257.sccc.jaxb.binding.users.User;
import io.grimlock257.sccc.jaxb.binding.users.Users;
import io.grimlock257.sccc.sharebrokering.manager.StocksFileManager;
import io.grimlock257.sccc.sharebrokering.manager.UsersFileManager;
import io.grimlock257.sccc.sharebrokering.model.FundsResponse;
import io.grimlock257.sccc.sharebrokering.model.LoginResponse;
import io.grimlock257.sccc.sharebrokering.model.UserStock;
import static io.grimlock257.sccc.sharebrokering.util.StringUtil.containsIgnoreCase;
import static io.grimlock257.sccc.sharebrokering.util.StringUtil.isNotNullOrEmpty;
import static io.grimlock257.sccc.sharebrokering.util.StringUtil.isNullOrEmpty;
import io.grimlock257.sccc.sharebrokering.util.UserUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.WebServiceException;

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
        return StocksFileManager.getInstance().unmarshal().getStocks();
    }

    /**
     * Unmarshalls the Stocks XML file and looks for a Stock with the provided stock symbol
     *
     * @param companySymbol The symbol to search for
     * @return The Stock object for the given companySymbol
     */
    @WebMethod(operationName = "getStockBySymbol")
    public Stock getStockBySymbol(
            @WebParam(name = "companySymbol") String companySymbol
    ) {

        // Only attempt to find stock by symbol if a symbol was supplied
        if (isNotNullOrEmpty(companySymbol)) {
            Stocks stocks = StocksFileManager.getInstance().unmarshal();

            for (Stock stock : stocks.getStocks()) {
                if (stock.getStockSymbol().equalsIgnoreCase(companySymbol)) {
                    return stock;
                }
            }
        }

        return null;
    }

    /**
     * Retrieve stocks information for a given user
     *
     * @param guid The GUID of the user whose shares to retrieve information for
     * @return A List object containing Stock objects
     */
    @WebMethod(operationName = "getUserStocks")
    public List<UserStock> getUserStocks(
            @WebParam(name = "guid") String guid
    ) {

        // Set up list to store user stocks
        List<UserStock> userStocks = new ArrayList<>();

        // Validate parameters
        if (isNullOrEmpty(guid)) {
            return userStocks;
        }

        // Check username is present in the system
        Users users = UsersFileManager.getInstance().unmarshal();

        for (User user : users.getUsers()) {
            if (user.getGuid().equalsIgnoreCase(guid)) {
                List<Share> userShares = user.getShares();

                if (userShares.isEmpty()) {
                    break;
                }

                // User has some shares, retrieve stock information from stocks file
                Stocks stocks = StocksFileManager.getInstance().unmarshal();

                for (Share share : userShares) {
                    for (Stock stock : stocks.getStocks()) {
                        if (stock.getStockSymbol().equalsIgnoreCase(share.getStockSymbol())) {
                            UserStock userStock = new UserStock(stock, share, user.getCurrency());

                            userStocks.add(userStock);
                        }
                    }
                }

                return userStocks;
            }
        }

        return userStocks;
    }

    /**
     * Unmarshalls the Stocks XML file, then iterates over each stock, checking the symbol of that stock compared to the provided stock symbol, and that the amount of available shares is greater or
     * equal to the desired quantity to purchase. If both these criteria are met, reduce the available shares by the purchase quantity amount, and attempt to marshal the changed XML.
     *
     * @param guid The GUID of the user purchasing shares
     * @param companySymbol The symbol of the company to purchase shares from
     * @param quantity The amount of shares to purchase
     * @return Whether the operation was successful
     */
    @WebMethod(operationName = "purchaseShare")
    public Boolean purchaseShare(
            @WebParam(name = "guid") String guid,
            @WebParam(name = "companySymbol") String companySymbol,
            @WebParam(name = "quantity") double quantity
    ) {

        // Validate parameters
        if (isNullOrEmpty(guid) || isNullOrEmpty(companySymbol)) {
            return false;
        }

        if (quantity <= 0) {
            return false;
        }

        Stocks stocks = StocksFileManager.getInstance().unmarshal();

        for (Stock stock : stocks.getStocks()) {
            if (stock.getStockSymbol().equalsIgnoreCase(companySymbol) && stock.getAvailableShares() >= quantity) {
                if (UserUtils.tryAddStockToUser(guid, stock.getStockSymbol(), stock.getPrice(), quantity)) {
                    stock.setAvailableShares(stock.getAvailableShares() - quantity);

                    return StocksFileManager.getInstance().marshal(stocks);
                } else {
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * Unmarshalls the Stocks XML file, then iterates over each stock, checking the symbol of that stock compared to the provided stock symbol. If this criteria is met, then increase the available
     * shares by the sell quantity amount, and attempt to marshal the changed XML
     *
     * @param guid The GUID of the user selling shares
     * @param companySymbol The symbol of the company to sell shares to
     * @param quantity The amount of shares to sell
     * @return Whether the operation was successful
     */
    @WebMethod(operationName = "sellShare")
    public Boolean sellShare(
            @WebParam(name = "guid") String guid,
            @WebParam(name = "companySymbol") String companySymbol,
            @WebParam(name = "quantity") double quantity
    ) {

        // Validate parameters
        if (isNullOrEmpty(guid) || isNullOrEmpty(companySymbol)) {
            return false;
        }

        if (quantity <= 0) {
            return false;
        }

        Stocks stocks = StocksFileManager.getInstance().unmarshal();

        for (Stock stock : stocks.getStocks()) {
            if (stock.getStockSymbol().equalsIgnoreCase(companySymbol)) {
                if (UserUtils.trySellStockFromUser(guid, stock.getStockSymbol(), stock.getPrice(), quantity)) {
                    stock.setAvailableShares(stock.getAvailableShares() + quantity);

                    return StocksFileManager.getInstance().marshal(stocks);
                } else {
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * Unmarshalls the Stocks XML file, then iterates over each stock, checking it against a set of criteria. The checks within check if the stock has
     * <b>failed</b> against a criteria. If the end of the iteration is reached, the stock in question is added to a list to be returned to the caller. The results are finally sorted by the column and
     * order specified.
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

        // Set defaults
        if (isNullOrEmpty(sortBy)) {
            sortBy = "stockSymbol";
        }

        if (isNullOrEmpty(order)) {
            order = "desc";
        }

        // Unmarshal the stocks, and filter and sort as neeeded
        List<Stock> stocks = StocksFileManager.getInstance().unmarshal().getStocks();

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
     * Creates a new Stock object based on the provided values, then unmarshalls the Stocks XML file, retrieves the list and adds the new Stock object before marshalling the file again.
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

        // Validate parameters
        if (isNullOrEmpty(stockName) || isNullOrEmpty(stockSymbol)) {
            return false;
        }

        // Make sure stock symbol isn't already present in the system
        Stocks stocks = StocksFileManager.getInstance().unmarshal();
        List<Stock> stocksList = stocks.getStocks();

        if (stocksList.stream().anyMatch(stock -> stock.getStockSymbol().equalsIgnoreCase(stockSymbol))) {
            return false;
        }

        // Create new Stock object based on supplied information
        SharePrice sharePrice = new SharePrice();

        // Attempt to retrieve price for the stock price SOAP web service, if failure, return false as stock addition could not be completed
        try {
            StockPriceResponse stockPrice = getSharePrice(stockSymbol);

            sharePrice.setCurrency(stockPrice.getStockCurrency());
            sharePrice.setPrice(stockPrice.getStockPrice());
            sharePrice.setUpdated(stockPrice.getStockPriceTime());
        } catch (WebServiceException e) {
            System.err.println("[ShareBrokering JAX-WS] WebServiceException connecting to stock price SOAP service resulting in failure to add new share. " + e.getMessage());

            return false;
        }

        Stock stock = new Stock();
        stock.setAvailableShares(availableShares);
        stock.setStockName(stockName);
        stock.setStockSymbol(stockSymbol.toUpperCase());
        stock.setPrice(sharePrice);

        // Add the new Stock and marshall
        stocksList.add(stock);

        // Re-order the list
        stocksList.sort(Comparator.comparing(Stock::getStockName));

        return StocksFileManager.getInstance().marshal(stocks);
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

        // Validate parameters
        if (isNullOrEmpty(stockSymbol)) {
            return false;
        }

        // Unmarshall stocks, find the stock with the supplied symbol, remove the stock and remarshall
        Stocks stocks = StocksFileManager.getInstance().unmarshal();
        List<Stock> stocksList = stocks.getStocks();

        Stock theStock = stocksList
                .stream()
                .filter(stock -> stock.getStockSymbol().equalsIgnoreCase(stockSymbol))
                .findFirst()
                .get();

        boolean wasRemoved = stocksList.removeIf(stock -> stock.getStockSymbol().equalsIgnoreCase(stockSymbol));

        if (wasRemoved) {
            // See if any users owned the now deleted stock, and force a sale if so
            Users users = UsersFileManager.getInstance().unmarshal();

            List<User> usersWithShare = users.getUsers()
                    .stream()
                    .filter(user -> user.getShares()
                    .stream()
                    .anyMatch(share -> share.getStockSymbol().equalsIgnoreCase(stockSymbol)))
                    .collect(Collectors.toList());

            usersWithShare.forEach((user) -> {
                Share userShare = user.getShares()
                        .stream()
                        .filter(stock -> stock.getStockSymbol().equalsIgnoreCase(stockSymbol))
                        .findFirst()
                        .get();

                if (!UserUtils.trySellStockFromUser(user.getGuid(), stockSymbol, theStock.getPrice(), userShare.getQuantity())) {
                    System.err.println("[ShareBrokering JAX-WS] Error attempting to force sell user stock upon stock removal. User '" + user.getGuid() + "' has '" + theStock.getStockSymbol() + "' that aren't present in the system any more.");
                }
            });

            return StocksFileManager.getInstance().marshal(stocks);
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
        Stocks stocks = StocksFileManager.getInstance().unmarshal();
        List<Stock> stocksList = stocks.getStocks();

        boolean foundStock = false;
        boolean madeEdit = false;

        // Iterate over the list, if matching stock found, update information if provided
        for (Stock stock : stocksList) {
            if (stock.getStockSymbol().equalsIgnoreCase(currentStockSymbol)) {
                foundStock = true;

                if (isNotNullOrEmpty(newStockSymbol)) {
                    // Make sure newStockSymbol is not already present in the system
                    if (!currentStockSymbol.equals(newStockSymbol) && stocksList.stream().anyMatch(s -> s.getStockSymbol().equalsIgnoreCase(newStockSymbol))) {
                        break;
                    }

                    stock.setStockSymbol(newStockSymbol);
                    madeEdit = true;
                }

                if (isNotNullOrEmpty(stockName)) {
                    stock.setStockName(stockName);
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
            return StocksFileManager.getInstance().marshal(stocks);
        } else {
            return false;
        }
    }

    /**
     * Handle user registration
     *
     * @param firstName The first name of the new user
     * @param lastName The last name of the new user
     * @param username The unique username of the user
     * @param password The raw password of the user
     * @param currency The currency of the user funds
     * @return Whether user creation was successful or not
     */
    @WebMethod(operationName = "registerUser")
    public boolean registerUser(
            @WebParam(name = "firstName") String firstName,
            @WebParam(name = "lastName") String lastName,
            @WebParam(name = "username") String username,
            @WebParam(name = "password") String password,
            @WebParam(name = "currency") String currency
    ) {

        // Validate parameters
        if (isNullOrEmpty(firstName) || isNullOrEmpty(lastName) || isNullOrEmpty(username) || isNullOrEmpty(password) || isNullOrEmpty(currency)) {
            return false;
        }

        // Make sure username already present in the system
        Users users = UsersFileManager.getInstance().unmarshal();
        List<User> usersList = users.getUsers();

        if (usersList.stream().anyMatch(user -> user.getUsername().equalsIgnoreCase(username))) {
            return false;
        }

        // Create a MD5 hash of the password
        String hashedPassword = UserUtils.hashPassword(password);

        if (hashedPassword == null) {
            return false;
        }

        // Create the new User object, and set properties
        User user = new User();

        user.setGuid(UUID.randomUUID().toString());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setRole(Role.USER);
        user.setCurrency(currency.toUpperCase());
        user.setAvailableFunds(0);

        // Add the new User and marshall
        usersList.add(user);

        return UsersFileManager.getInstance().marshal(users);
    }

    /**
     * Attempt to validate the user details against the stored results
     *
     * @param username The provided username
     * @param password The provided password
     * @return A LoginResponse object containing whether it was successful or not, if so will contain GUID and role
     */
    @WebMethod(operationName = "loginUser")
    public LoginResponse loginUser(
            @WebParam(name = "username") String username,
            @WebParam(name = "password") String password
    ) {

        // Validate parameters
        if (isNullOrEmpty(username) || isNullOrEmpty(password)) {
            return LoginResponse.unsuccessfulResponse();
        }

        // Check username is present in the system
        Users users = UsersFileManager.getInstance().unmarshal();
        List<User> usersList = users.getUsers();

        // Attempt to find the user
        try {
            User user = usersList.stream().filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst().get();

            // If the user was found, hash the provided input and check against stored file
            String hashedPassword = UserUtils.hashPassword(password);

            if (user.getPassword().equals(hashedPassword)) {
                return LoginResponse.successfulResponse(user.getGuid(), user.getRole());
            } else {
                return LoginResponse.unsuccessfulResponse();
            }
        } catch (NoSuchElementException e) {
            return LoginResponse.unsuccessfulResponse();
        }
    }

    /**
     * Retrieve the available funds for the given user
     *
     * @param guid The user whose funds to retrieve
     * @return A FundsReponse object containing available funds and currency, or null if user not found
     */
    @WebMethod(operationName = "getUserFunds")
    public FundsResponse getUserFunds(
            @WebParam(name = "guid") String guid
    ) {

        // Validate parameters
        if (isNullOrEmpty(guid)) {
            return null;
        }

        // Check username is present in the system
        Users users = UsersFileManager.getInstance().unmarshal();
        List<User> usersList = users.getUsers();

        // Attempt to find the user
        try {
            User user = usersList.stream().filter(u -> u.getGuid().equalsIgnoreCase(guid)).findFirst().get();

            return new FundsResponse(user.getAvailableFunds(), user.getCurrency());
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /**
     * Deposit an amount to the users available funds
     *
     * @param guid The GUID of the account to deposit funds to
     * @param amount The amount of funds to deposit
     * @return Whether the deposit was successful or not
     */
    @WebMethod(operationName = "depositFunds")
    public boolean depositFunds(
            @WebParam(name = "guid") String guid,
            @WebParam(name = "amount") double amount
    ) {

        // Validate parameters
        if (isNullOrEmpty(guid)) {
            return false;
        }

        // Disallow depositing negative amounts and zero
        if (amount <= 0) {
            return false;
        }

        // Check username is present in the system
        Users users = UsersFileManager.getInstance().unmarshal();
        List<User> usersList = users.getUsers();

        // Attempt to find the user
        try {
            User user = usersList.stream().filter(u -> u.getGuid().equalsIgnoreCase(guid)).findFirst().get();

            user.setAvailableFunds(user.getAvailableFunds() + amount);

            return UsersFileManager.getInstance().marshal(users);
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Withdraw an amount from the users available funds
     *
     * @param guid The GUID of the account to withdraw funds to
     * @param amount The amount of funds to withdraw
     * @return Whether the withdrawal was successful or not
     */
    @WebMethod(operationName = "withdrawFunds")
    public boolean withdrawFunds(
            @WebParam(name = "guid") String guid,
            @WebParam(name = "amount") double amount
    ) {

        // Validate parameters
        if (isNullOrEmpty(guid)) {
            return false;
        }

        // Disallow withdrawing negative amounts and zero
        if (amount <= 0) {
            return false;
        }

        // Check username is present in the system
        Users users = UsersFileManager.getInstance().unmarshal();
        List<User> usersList = users.getUsers();

        // Attempt to find the user
        try {
            User user = usersList.stream().filter(u -> u.getGuid().equalsIgnoreCase(guid)).findFirst().get();

            double availableFunds = user.getAvailableFunds();

            if (availableFunds >= amount) {
                user.setAvailableFunds(user.getAvailableFunds() - amount);

                return UsersFileManager.getInstance().marshal(users);
            } else {
                return false;
            }
        } catch (NoSuchElementException e) {
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
