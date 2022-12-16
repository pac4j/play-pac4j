package org.pac4j.play.context;

import lombok.Getter;
import org.pac4j.core.context.FrameworkParameters;
import play.api.mvc.RequestHeader;
import play.mvc.Http;
/**
 * Play framework parameters
 *
 * @author Jerome LELEU
 * @since 12.0.0
 */
@Getter
public class PlayFrameworkParameters implements FrameworkParameters {

    private Http.RequestHeader javaRequest;

    private RequestHeader scalaRequest;

    public PlayFrameworkParameters(final Http.RequestHeader javaRequest) {
        this.javaRequest = javaRequest;
    }

    public PlayFrameworkParameters(final RequestHeader scalaRequest) {
        this.scalaRequest = scalaRequest;
    }
}
