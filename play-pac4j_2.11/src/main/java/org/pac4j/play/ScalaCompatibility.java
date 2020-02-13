package org.pac4j.play;

import play.api.mvc.AnyContentAsFormUrlEncoded;
import scala.collection.JavaConversions;
import scala.collection.Seq;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Karel Cemus
 */
class ScalaCompatibility {

    static Map<String, String[]> parseBody(final AnyContentAsFormUrlEncoded body) {
        final Map<String, String[]> p = new HashMap<>();
        final scala.collection.immutable.Map<String, Seq<String>> scalaParameters = body.asFormUrlEncoded().get();
        for (final String key : JavaConversions.setAsJavaSet(scalaParameters.keySet())) {
            final Seq<String> v = scalaParameters.get(key).get();
            final String[] values = new String[v.size()];
            v.copyToArray(values);
            p.put(key, values);
        }
        return p;
    }
}
