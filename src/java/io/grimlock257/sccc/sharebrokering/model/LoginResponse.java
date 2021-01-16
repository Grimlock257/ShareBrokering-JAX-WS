package io.grimlock257.sccc.sharebrokering.model;

import io.grimlock257.sccc.jaxb.binding.users.Role;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Represents a login response model to return to the clients
 *
 * @author AdamW
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class LoginResponse {

    private boolean successful;
    private String guid;
    private Role role;

    public LoginResponse() {
        this.successful = false;
        this.guid = null;
        this.role = Role.USER;
    }

    private LoginResponse(boolean successful, String guid, Role role) {
        this.successful = successful;
        this.guid = guid;
        this.role = role;
    }

    public static LoginResponse successfulResponse(String guid, Role role) {
        return new LoginResponse(true, guid, role);
    }

    public static LoginResponse unsuccessfulResponse() {
        return new LoginResponse(false, null, Role.USER);
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getGuid() {
        return guid;
    }

    public Role getRole() {
        return role;
    }
}
