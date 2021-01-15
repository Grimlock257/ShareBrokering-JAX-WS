package io.grimlock257.sccc.sharebrokering.manager;

import io.grimlock257.sccc.jaxb.binding.users.Users;
import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * UsersFileManager

 This class handles marshalling and unmarshalling of the users XML files
 *
 * @author Adam Watson
 */
public class UsersFileManager {

    private static UsersFileManager instance = null;

    private final String xmlFileLocation;

    private final Object fileLock = new Object();

    /**
     * JAXBFileManager constructor
     *
     * Private to enforce singleton behaviour
     */
    private UsersFileManager() {
        xmlFileLocation = "users.xml";
    }

    /**
     * Get the instance of the UsersFileManager singleton
     *
     * @return The instance of the UsersFileManager
     */
    public static UsersFileManager getInstance() {

        if (instance == null) {
            instance = new UsersFileManager();
        }

        return instance;
    }

    /**
     * Marshal the provided Users object to the local XML file
     *
     * @param users The Users object to be marshaled
     * @return Whether the marshal was successful or not
     */
    public Boolean marshal(Users users) {

        try {

            JAXBContext jaxbContext = JAXBContext.newInstance(Users.class.getPackage().getName());

            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            // Apply a file lock at this point so that when the file is being marshaled, no other
            // file operations can happen at the same time
            synchronized (fileLock) {
                marshaller.marshal(users, new File(xmlFileLocation));
            }
        } catch (JAXBException e) {

            System.err.println("[ERROR] Could not create JAXBContext instance: " + e.getMessage());

            return false;
        }

        return true;
    }

    /**
     * Unmarshal the list and return the root Users element
     *
     * @return The unmarshalled Users object
     */
    public Users unmarshal() {

        try {

            JAXBContext jaxbContext = JAXBContext.newInstance(Users.class.getPackage().getName());

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            Users users;

            // Apply a file lock at this point so that when the file is being unmarshalled, no other
            // file operations can happen at the same time
            synchronized (fileLock) {
                users = (Users) unmarshaller.unmarshal(new File(xmlFileLocation));
            }

            return users;
        } catch (JAXBException e) {
            System.err.println("[ERROR] Could not create JAXBContext instance: " + e.getMessage());
        }

        return null;
    }
}
