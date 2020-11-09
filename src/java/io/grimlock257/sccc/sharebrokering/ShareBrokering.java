package io.grimlock257.sccc.sharebrokering;

import io.grimlock257.sccc.jaxb.binding.Stock;
import io.grimlock257.sccc.sharebrokering.manager.JAXBFileManager;
import java.util.List;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 * @author Adam Watson
 */
@WebService(serviceName = "ShareBrokering")
@Stateless()
public class ShareBrokering {

    @WebMethod(operationName = "getAllStocks")
    public List<Stock> getAllStocks() {
        return JAXBFileManager.getInstance().unmarshal().getStocks();
    }
}
