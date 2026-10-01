package org.pac4j.play.result;

import lombok.Getter;
import org.pac4j.play.PlayWebContext;
import play.mvc.Result;

/**
 * Result holding a Play web context.
 *
 * @author Jerome LELEU
 * @since 12.0.0
 */
public class PlayWebContextResultHolder extends Result {

    @Getter
    private final PlayWebContext playWebContext;

    public PlayWebContextResultHolder(final PlayWebContext playWebContext) {
        super(0);
        this.playWebContext = playWebContext;
    }
}
