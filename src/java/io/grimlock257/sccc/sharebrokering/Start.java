package io.grimlock257.sccc.sharebrokering;

import io.github.grimlock257.stocks.StockPrice;
import io.github.grimlock257.stocks.StockPriceResponse;
import io.github.grimlock257.stocks.StockPriceSoap;
import io.grimlock257.sccc.jaxb.binding.SharePrice;
import io.grimlock257.sccc.jaxb.binding.Stock;
import io.grimlock257.sccc.jaxb.binding.Stocks;
import io.grimlock257.sccc.jaxb.binding.users.Role;
import io.grimlock257.sccc.jaxb.binding.users.User;
import io.grimlock257.sccc.jaxb.binding.users.Users;
import io.grimlock257.sccc.sharebrokering.jobs.StockPriceUpdater;
import io.grimlock257.sccc.sharebrokering.manager.StocksFileManager;
import io.grimlock257.sccc.sharebrokering.manager.UsersFileManager;
import io.grimlock257.sccc.sharebrokering.util.UserUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.xml.ws.WebServiceException;

/**
 * @author Adam Watson
 */
@Singleton
@Startup
public class Start {

    /**
     * This method will run before the web service container starts up
     */
    @PostConstruct
    public void init() {
        setupDummyData();
        createInitialUsersFile();

        StockPriceUpdater.initiate();
    }

    /**
     * This method will run before the web service shuts down
     */
    @PreDestroy
    public void destroy() {
        StockPriceUpdater.getInstance().cancel();
    }

    /**
     * Set up dummy data at web service start if no shares.xml file is found
     */
    private void setupDummyData() {
        if (StocksFileManager.getInstance().unmarshal() == null) {
            // Create new Stocks object that will be marshalled
            Stocks stocks = new Stocks();
            List<Stock> stocksList = stocks.getStocks();

            // Create initial list of dumb stocks
            List<InitialStock> initialStocks = new ArrayList<>();

            initialStocks.add(new InitialStock("Amazon", "AMZN", 1000.99));
            initialStocks.add(new InitialStock("Apple", "AAPL", 500.0));
            initialStocks.add(new InitialStock("Applegreen", "APGN.IR", 50.0));
            initialStocks.add(new InitialStock("Coca-cola", "KO", 100.5));
            initialStocks.add(new InitialStock("National Grid", "NG.L", 25.25));
            initialStocks.add(new InitialStock("Netflix", "NFLX", 75.01));
            initialStocks.add(new InitialStock("Nvidia", "NVDA", 3090.0));
            initialStocks.add(new InitialStock("Tesla", "TSLA", 5000.0));
            initialStocks.add(new InitialStock("TomTom", "TOM2.AS", 250.0));

            // Iterate over the list of initial stock data and create proper stock objects from them
            for (InitialStock initialStock : initialStocks) {
                StockPriceResponse stockPrice;

                try {
                    stockPrice = getSharePrice(initialStock.getStockSymbol());
                } catch (WebServiceException e) {
                    System.err.println("[ShareBrokering JAX-WS] WebServiceException connecting to stock price SOAP service resulting in failure to retrieve initial stock price. " + e.getMessage());

                    continue;
                }

                SharePrice sharePrice = new SharePrice();
                sharePrice.setCurrency(stockPrice.getStockCurrency());
                sharePrice.setPrice(stockPrice.getStockPrice());
                sharePrice.setUpdated(stockPrice.getStockPriceTime());

                Stock stock = new Stock();
                stock.setAvailableShares(initialStock.getShareAmount());
                stock.setStockName(initialStock.getStockName());
                stock.setStockSymbol(initialStock.getStockSymbol());
                stock.setPrice(sharePrice);

                stocksList.add(stock);
            }

            // Re-order the list
            stocksList.sort(Comparator.comparing(Stock::getStockName));

            // Marshal the initial stock list
            StocksFileManager.getInstance().marshal(stocks);
        }
    }

    /**
     * Private class as only used for dummy data creation
     */
    private class InitialStock {

        private final String stockName;
        private final String stockSymbol;
        private final double shareAmount;

        /**
         * Represent a stock object for the initial creation
         *
         * @param stockName The stock name
         * @param stockSymbol The stock symbol
         * @param shareAmount The amount of shares
         */
        public InitialStock(String stockName, String stockSymbol, double shareAmount) {
            this.stockName = stockName;
            this.stockSymbol = stockSymbol;
            this.shareAmount = shareAmount;
        }

        public String getStockName() {
            return stockName;
        }

        public String getStockSymbol() {
            return stockSymbol;
        }

        public double getShareAmount() {
            return shareAmount;
        }
    }

    /**
     * Create initial users file if it doesn't already exist
     */
    private void createInitialUsersFile() {
        if (UsersFileManager.getInstance().unmarshal() == null) {
            // Create new Stocks object that will be marshalled
            Users users = new Users();
            List<User> usersList = users.getUsers();

            // Create a admin user - for submission purposes only
            User user = new User();

            // Create hashed password for this user
            String hashedPassword = UserUtils.hashPassword("admin");

            user.setGuid(UUID.randomUUID().toString());
            user.setFirstName("Admin");
            user.setLastName("Admin");
            user.setUsername("Admin");
            user.setPassword(hashedPassword);
            user.setRole(Role.ADMIN);
            user.setCurrency("GBP");
            user.setAvailableFunds(0);

            // Add the new User and marshall
            usersList.add(user);

            UsersFileManager.getInstance().marshal(users);
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
