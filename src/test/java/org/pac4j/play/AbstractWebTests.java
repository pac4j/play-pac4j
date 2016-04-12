package org.pac4j.play;

import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.util.TestsConstants;
import play.core.j.JavaResultExtractor;
import play.mvc.Result;

import java.io.IOException;

/**
 * Some utility methods for tests.
 *
 * @author Jerome Leleu
 * @since 2.2.0
 */
public abstract class AbstractWebTests implements TestsConstants {

    protected String getBody(final Result result) throws IOException {
        return new String(JavaResultExtractor.getBody(result, 0L), HttpConstants.UTF8_ENCODING);
    }
}
