package org.pac4j.play.deadbolt2;

import be.objectify.deadbolt.java.models.Permission;
import be.objectify.deadbolt.java.models.Role;
import be.objectify.deadbolt.java.models.Subject;
import org.pac4j.core.profile.CommonProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * The deadbolt subject built from the pac4j user profile.
 *
 * @author Jerome Leleu
 * @since 2.6.0
 */
public class Pac4jSubject implements Subject {

    private String id;

    private List<Pac4jRole> roles = new ArrayList<>();

    private List<Pac4jPermission> permissions = new ArrayList<>();

    public Pac4jSubject(final CommonProfile profile) {
        id = profile.getId();
        for (final String role : profile.getRoles()) {
            roles.add(new Pac4jRole(role));
        }
        for (final String permission : profile.getPermissions()) {
            permissions.add(new Pac4jPermission(permission));
        }
    }

    @Override
    public List<? extends Role> getRoles() {
        return roles;
    }

    @Override
    public List<? extends Permission> getPermissions() {
        return permissions;
    }

    @Override
    public String getIdentifier() {
        return id;
    }
}
