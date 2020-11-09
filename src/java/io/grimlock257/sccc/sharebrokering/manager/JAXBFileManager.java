package io.grimlock257.sccc.sharebrokering.manager;

import io.grimlock257.sccc.jaxb.binding.Stocks;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * JAXBFileManager
 *
 * This class handles marshalling and unmarshalling of the stocks XML files
 *
 * @author Adam Watson
 */
public class JAXBFileManager {

    private static JAXBFileManager instance = null;

    private final String xmlFileLocation;

    private final Object fileLock = new Object();

    /**
     * JAXBFileManager constructor
     *
     * Private to enforce singleton behaviour
     */
    private JAXBFileManager() {
        xmlFileLocation = "stocks.xml";
    }

    /**
     * Get the instance of the JAXBFileManager singleton
     *
     * @return The instance of the JAXBFileManager
     */
    public static JAXBFileManager getInstance() {

        if (instance == null) {
            instance = new JAXBFileManager();
        }

        return instance;
    }

    /**
     * Marshal the provided Stocks object to the local XML file
     *
     * @param stocks The Stocks object to be marshaled
     */
    public void marshal(Stocks stocks) {

        try {

            JAXBContext jaxbContext = JAXBContext.newInstance(Stocks.class.getPackage().getName());

            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            // Apply a file lock at this point so that when the file is being marshaled, no other
            // file operations can happen at the same time
            synchronized (fileLock) {
                marshaller.marshal(stocks, new File(xmlFileLocation));
            }
        } catch (JAXBException e) {
            System.err.println("[ERROR} Could not create JAXBContext instance: " + e.getMessage());
        }
    }

    /**
     * Unmarshal the list and return the root Stocks element
     *
     * @return The unmarshalled Stocks object
     */
    public Stocks unmarshal() {

        try {

            JAXBContext jaxbContext = JAXBContext.newInstance(Stocks.class.getPackage().getName());

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            Stocks stocks;

            // Apply a file lock at this point so that when the file is being unmarshalled, no other
            // file operations can happen at the same time
            synchronized (fileLock) {
                stocks = (Stocks) unmarshaller.unmarshal(new File(xmlFileLocation));
            }

            return stocks;
        } catch (JAXBException e) {
            System.err.println("[ERROR} Could not create JAXBContext instance: " + e.getMessage());
        }

        return null;
    }
}
