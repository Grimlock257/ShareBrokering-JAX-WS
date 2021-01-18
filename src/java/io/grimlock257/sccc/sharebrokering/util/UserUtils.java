package io.grimlock257.sccc.sharebrokering.util;

import io.grimlock257.sccc.jaxb.binding.SharePrice;
import io.grimlock257.sccc.jaxb.binding.users.Share;
import io.grimlock257.sccc.jaxb.binding.users.User;
import io.grimlock257.sccc.jaxb.binding.users.Users;
import io.grimlock257.sccc.sharebrokering.manager.UsersFileManager;
import io.grimlock257.sccc.sharebrokering.service.CurrencyConverterAPIService;
import java.util.Iterator;
import java.util.List;

/**
 * Stores methods related to working with user data
 *
 * @author Adam Watson
 */
public class UserUtils {

    /**
     * Add Share(s) to a user
     *
     * @param guid The GUID of the user purchasing a stock
     * @param stockSymbol The stock symbol for the purchased share
     * @param sharePriceInformation The SharePrice object for the stock to be purchased
     * @param quantity The quantity of shares purchased
     * @return Whether the purchase was successful or not
     */
    public static boolean tryAddStockToUser(String guid, String stockSymbol, SharePrice sharePriceInformation, double quantity) {
        Users users = UsersFileManager.getInstance().unmarshal();

        // Iterate over list and look for matching GUID
        for (User user : users.getUsers()) {
            if (user.getGuid().equalsIgnoreCase(guid)) {
                // Check the user has enough funds to complete the purchase
                String userFundsCurrency = user.getCurrency();
                String stockListCurrency = sharePriceInformation.getCurrency();
                double stockListPrice = sharePriceInformation.getPrice();

                double purchasePrice = CurrencyConverterAPIService.getInstance().convertCurrency(stockListCurrency, userFundsCurrency, stockListPrice * quantity);

                if (purchasePrice < 0 || purchasePrice > user.getAvailableFunds()) {
                    break;
                }

                user.setAvailableFunds(user.getAvailableFunds() - purchasePrice);

                // User has funds, proceed with purchase
                boolean hasShareAlready = false;

                List<Share> userShares = user.getShares();

                // Check if user already owns shares for the provided symbol, if so edit existing, otherwise add new entry
                for (Share share : userShares) {
                    if (share.getStockSymbol().equalsIgnoreCase(stockSymbol)) {
                        hasShareAlready = true;

                        share.setQuantity(share.getQuantity() + quantity);
                        share.setPurchaseValue(share.getPurchaseValue() + purchasePrice);

                        break;
                    }
                }

                if (!hasShareAlready) {
                    Share share = new Share();

                    share.setStockSymbol(stockSymbol);
                    share.setPurchaseValue(purchasePrice);
                    share.setQuantity(quantity);

                    userShares.add(share);
                }

                return UsersFileManager.getInstance().marshal(users);
            }
        }

        return false;
    }

    /**
     * Remove Share(s) from a user
     *
     * @param guid The GUID of the user selling a stock
     * @param stockSymbol The stock symbol for the sold share
     * @param sharePriceInformation The SharePrice object for the stock to be sold
     * @param quantity The quantity of shares sold
     * @return Whether the sale was successful or not
     */
    public static boolean trySellStockFromUser(String guid, String stockSymbol, SharePrice sharePriceInformation, double quantity) {
        Users users = UsersFileManager.getInstance().unmarshal();

        // Iterate over list and look for matching GUID
        for (User user : users.getUsers()) {
            if (user.getGuid().equalsIgnoreCase(guid)) {
                boolean hasShareAlready = false;

                Iterator<Share> userSharesIterator = user.getShares().iterator();

                // Iterate over owned shares, looking for stock symbol match, and that the user owns the same as or more than the quantity attempted to be sold
                while (userSharesIterator.hasNext()) {
                    Share share = userSharesIterator.next();

                    // User has enough to sell
                    if (share.getStockSymbol().equalsIgnoreCase(stockSymbol) && share.getQuantity() >= quantity) {
                        // Convert price
                        String userFundsCurrency = user.getCurrency();
                        String stockListCurrency = sharePriceInformation.getCurrency();
                        double stockListPrice = sharePriceInformation.getPrice();

                        double salePrice = CurrencyConverterAPIService.getInstance().convertCurrency(stockListCurrency, userFundsCurrency, stockListPrice * quantity);

                        if (salePrice < 0) {
                            break;
                        }

                        user.setAvailableFunds(user.getAvailableFunds() + salePrice);

                        hasShareAlready = true;

                        // The sale results in the user owning no shares, remove the entry, otherwise edit the existing entry
                        if (share.getQuantity() - quantity == 0) {
                            userSharesIterator.remove();
                        } else {
                            share.setQuantity(share.getQuantity() - quantity);
                            share.setPurchaseValue(share.getPurchaseValue() - salePrice);
                        }

                        break;
                    }
                }

                // Attempted to sell a share the user does not own, don't bother marshalling
                if (!hasShareAlready) {
                    return false;
                }

                return UsersFileManager.getInstance().marshal(users);
            }
        }

        return false;
    }
}