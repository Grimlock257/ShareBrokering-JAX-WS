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
                    StockPriceResponse stockPrice = getSharePrice(stock.getStockSymbol());

                    SharePrice sharePrice = new SharePrice();
                    sharePrice.setCurrency(stockPrice.getStockCurrency());
                    sharePrice.setPrice(stockPrice.getStockPrice());
                    sharePrice.setUpdated(stockPrice.getStockPriceTime());

                    stock.setPrice(sharePrice);
                }

                boolean result = JAXBFileManager.getInstance().marshal(stocks);

                if (!result) {
                    System.err.println("Error marshalling updated stock price file");
                }

                System.out.println("Stock price update successful");
            }
        }, STOCK_PRICE_INITIAL_DELAY, STOCK_PRICE_UPDATE_FREQUENCY);
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
