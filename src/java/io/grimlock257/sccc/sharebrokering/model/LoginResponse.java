package io.grimlock257.sccc.sharebrokering.model;

import io.grimlock257.sccc.jaxb.binding.users.Role;

/**
 * Represents a login response model to return to the clients
 *
 * @author AdamW
 */
public class LoginResponse {

    private boolean successful;
    private String guid;
    private Role role;

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
}
