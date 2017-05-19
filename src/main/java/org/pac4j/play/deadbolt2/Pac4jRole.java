package org.pac4j.play.deadbolt2;

import be.objectify.deadbolt.java.models.Role;
import org.pac4j.core.util.CommonHelper;

/**
 * A simple role from pac4j.
 *
 * @author Jerome Leleu
 * @since 2.6.0
 */
public class Pac4jRole implements Role {

    private final String name;

    public Pac4jRole(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return CommonHelper.toString(this.getClass(), "name", name);
    }
}
