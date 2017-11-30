package org.pac4j.play;

import scala.collection.JavaConverters;

/**
 * @author Karel Cemus
 */
class ScalaCompatibility {

    static <T> java.util.Set<T> scalaSetToJavaSet(scala.collection.immutable.Set<T> set) {
        return JavaConverters.setAsJavaSet(set);
    }
}
