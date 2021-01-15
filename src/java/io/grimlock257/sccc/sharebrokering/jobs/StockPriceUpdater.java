package io.grimlock257.sccc.sharebrokering.jobs;

import io.github.grimlock257.stocks.StockPrice;
import io.github.grimlock257.stocks.StockPriceResponse;
import io.github.grimlock257.stocks.StockPriceSoap;
import io.grimlock257.sccc.jaxb.binding.SharePrice;
import io.grimlock257.sccc.jaxb.binding.Stock;
import io.grimlock257.sccc.jaxb.binding.Stocks;
import io.grimlock257.sccc.sharebrokering.manager.JAXBFileManager;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.xml.ws.WebServiceException;

/**
 * StockPriceUpdater
 *
 * Singleton class to handle the stock price updater task
 *
 * @author Adam Watson
 */
public class StockPriceUpdater {

    private static StockPriceUpdater instance = null;

    private Timer stockPriceUpdaterTime;

    private final int STOCK_PRICE_INITIAL_DELAY = 10 * 1000;
    private final int STOCK_PRICE_UPDATE_FREQUENCY = 30 * 1000;

    /**
     * StockPriceUpdater constructor
     *
     * Private to enforce singleton behaviour
     */
    private StockPriceUpdater() {
        setupStockPriceUpdaterTask();
    }

    /**
     * Initiate the stock price updater if not already
     */
    public static void initiate() {

        if (instance == null) {
            instance = new StockPriceUpdater();
        }
    }

    /**
     * Get the instance of the StockPriceUpdater singleton
     *
     * @return The instance of the StockPriceUpdater
     */
    public static StockPriceUpdater getInstance() {

        if (instance == null) {
            instance = new StockPriceUpdater();
        }

        return instance;
    }

    /**
     * Set up stock price updater task to run every STOCK_PRICE_UPDATE_FREQUENCY (in seconds) to retrieve the up to date stock price information from the remote web service
     */
    private void setupStockPriceUpdaterTask() {
        stockPriceUpdaterTime = new Timer();

        stockPriceUpdaterTime.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Updating stock prices...");

                Stocks stocks = JAXBFileManager.getInstance().unmarshal();
                List<Stock> stocksList = stocks.getStocks();

                for (Stock stock : stocksList) {
                    StockPriceResponse stockPrice;

                    try {
                        stockPrice = getSharePrice(stock.getStockSymbol());
                    } catch (WebServiceException e) {
                        System.err.println("WebServiceException connecting to stock price SOAP service resulting in failure to update stock price. " + e.getMessage());

                        continue;
                    }

                    SharePrice sharePrice = new SharePrice();
                    sharePrice.setCurrency(stockPrice.getStockCurrency());
                    sharePrice.setPrice(stockPrice.getStockPrice());
                    sharePrice.setUpdated(stockPrice.getStockPriceTime());

                    stock.setPrice(sharePrice);
                }

                boolean result = JAXBFileManager.getInstance().marshal(stocks);

                if (result) {
                    System.out.println("Stock price update successful");
                } else {
                    System.err.println("Error marshalling updated stock price file");
                }
            }
        }, STOCK_PRICE_INITIAL_DELAY, STOCK_PRICE_UPDATE_FREQUENCY);
    }

    /**
     * Forcefully cancel the timer task
     */
    public void cancel() {
        stockPriceUpdaterTime.cancel();
        stockPriceUpdaterTime.purge();
        stockPriceUpdaterTime = null;
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
