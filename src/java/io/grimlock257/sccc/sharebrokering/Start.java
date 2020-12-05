package io.grimlock257.sccc.sharebrokering;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * @author Adam Watson
 */
@Singleton
@Startup
public class Start {

    @PostConstruct
    public void init() {
        // This method runs at startup
    }
}
