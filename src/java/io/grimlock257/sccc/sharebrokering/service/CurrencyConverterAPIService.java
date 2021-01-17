package io.grimlock257.sccc.sharebrokering.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * CurrencyConverterAPIService
 *
 * This class handles calling out to the external currency converter API service
 *
 * @author Adam Watson
 */
public class CurrencyConverterAPIService {

    private static CurrencyConverterAPIService instance = null;

    /**
     * CurrencyConverterAPIService constructor
     *
     * Private to enforce singleton behaviour
     */
    private CurrencyConverterAPIService() {
    }

    /**
     * Get the instance of the CurrencyConverterAPIService singleton
     *
     * @return The instance of the CurrencyConverterAPIService
     */
    public static CurrencyConverterAPIService getInstance() {

        if (instance == null) {
            instance = new CurrencyConverterAPIService();
        }

        return instance;
    }

    /**
     * Convert the provided value from its currency to a target currency via calling an external API
     *
     * @param baseCurrency The currency the provided sourceValue is in
     * @param targetCurrency The desired resulting currency
     * @param sourceValue The value to convert
     * @return The converted price, or -1 if an error occurred
     */
    public double convertCurrency(String baseCurrency, String targetCurrency, double sourceValue) {
        try {
            // Request components
            String baseUrl = "http://localhost:8080/CurrencyAPI/webresources/convert";
            String apiQueryParam = "?baseCurrency=" + baseCurrency + "&targetCurrency=" + targetCurrency + "&value=" + sourceValue;

            // Create URL object
            URL url = new URL(baseUrl + apiQueryParam);

            // Create HTTP connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // If the response was not a 200, throw an error
            if (conn.getResponseCode() != 200) {
                throw new IOException(conn.getResponseMessage());
            }

            // Retrieve the connection input stream and store as a JsonObject
            JsonReader jsonReader = Json.createReader(conn.getInputStream());
            JsonObject jsonObject = jsonReader.readObject();
            String jsonStatusValue = jsonObject.getJsonString("status").getString();

            if (!jsonStatusValue.equalsIgnoreCase("success")) {
                return -1;
            }

            return jsonObject.getJsonNumber("value").doubleValue();
        } catch (MalformedURLException e) {
            System.err.println("[ShareBrokering JAX-WS] Malformed URL: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("[ShareBrokering JAX-WS] IOException connecting to URL: " + e.getMessage());
        }

        return -1;
    }
}
