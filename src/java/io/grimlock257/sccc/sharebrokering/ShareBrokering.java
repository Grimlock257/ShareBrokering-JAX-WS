package io.grimlock257.sccc.sharebrokering;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.ejb.Stateless;

/**
 * @author Adam Watson
 */
@WebService(serviceName = "ShareBrokering")
@Stateless()
public class ShareBrokering {

    @WebMethod(operationName = "hello")
    public String hello(@WebParam(name = "name") String txt) {
        return "Hello " + txt + " !";
    }
}
