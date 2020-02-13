package org.pac4j.play;

import play.api.mvc.AnyContentAsFormUrlEncoded;
import scala.collection.immutable.Seq;
import scala.jdk.CollectionConverters;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jerome LELEU
 * @since 9.0.0
 */
class ScalaCompatibility {

    static Map<String, String[]> parseBody(final AnyContentAsFormUrlEncoded body) {
        final Map<String, String[]> p = new HashMap<>();
        final scala.collection.immutable.Map<String, Seq<String>> scalaParameters = body.asFormUrlEncoded().get();
        for (final String key : CollectionConverters.SetHasAsJava(scalaParameters.keySet()).asJava()) {
            final Seq<String> v = scalaParameters.get(key).get();
            final String[] values = new String[v.size()];
            v.copyToArray(values);
            p.put(key, values);
        }
        return p;
    }
}
