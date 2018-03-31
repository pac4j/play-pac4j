package org.pac4j.play.deadbolt2;

import be.objectify.deadbolt.java.models.Permission;
import org.pac4j.core.util.CommonHelper;

/**
 * A simple permission from pac4j.
 *
 * @author Jerome Leleu
 * @since 2.6.0
 */
public class Pac4jPermission implements Permission {

    private final String value;

    public Pac4jPermission(final String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return CommonHelper.toNiceString(this.getClass(), "value", value);
    }
}
