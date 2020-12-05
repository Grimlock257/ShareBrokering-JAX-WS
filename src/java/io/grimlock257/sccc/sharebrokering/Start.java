package io.grimlock257.sccc.sharebrokering;

import io.grimlock257.sccc.sharebrokering.jobs.StockPriceUpdater;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

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
        StockPriceUpdater.initiate();
    }
}
