/*
  Copyright 2012 - 2015 pac4j organization

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.pac4j.play.store;

import org.pac4j.play.PlayWebContext;

/**
 * An abstraction to manage data in a store.
 *
 * @author Jerome Leleu
 * @since 2.0.0
 */
public interface DataStore {

    /**
     * Get or create the session identifier and initialize the session with it if necessary.
     *
     * @param context
     * @return the session identifier
     */
    String getOrCreateSessionId(PlayWebContext context);

    /**
     * Get the object from its key in store.
     *
     * @param context
     * @param key
     * @return
     */
    Object get(PlayWebContext context, String key);

    /**
     * Save an object in the store by its key.
     *
     * @param context
     * @param key
     * @param value
     */
    void set(PlayWebContext context, String key, Object value);

    /**
     * Invalidate the current store.
     *
     * @param context
     */
    void invalidate(PlayWebContext context);
}
