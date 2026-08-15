/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.apache.directory.studio.openldap.config.acl;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO CONSULTING THE PROTOCOL DATABANKS ────────────────
// C-3PO fluent in over six million forms of communication, cross-referencing
// the correct phrase for a given situation by key code. This class does exactly
// that: given a message key, we look it up in our .properties resource bundle
// and hand back the human-readable string.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Utility class for loading internationalised (i18n) UI strings from our
 * resource bundle. Think of it as a tiny lookup table — we ask by key, we get
 * back text the UI can display.
 * Think of this class as C-3PO flicking through his protocol databanks by
 * reference code and reading out the matching phrase in whatever language
 * the situation calls for.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class Messages
{
    private static final String BUNDLE_NAME = "org.apache.directory.studio.openldap.config.acl.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );


    // ── No Construction Allowed ───────────────────────────────────────────────
    // C-3PO doesn't hand you his databanks — he just answers queries.
    // We make this class purely static for the same reason: no state, no
    // instances, just a lookup service.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor — this class is a pure static utility. We never
     * create instances of it, just call {@link #getString(String)} directly.
     * Why? Because there is no state to maintain; it is a stateless lookup.
     */
    private Messages()
    {
    }


    // ── Consulting the Protocol Databanks ─────────────────────────────────────
    // C-3PO receives a reference code from the crew and searches his databanks.
    // If the code matches a known phrase, he reads it out clearly.
    // If the code is unknown he announces it wrapped in exclamation marks so
    // everyone knows something went wrong.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a UI string by its message key. We pull the value straight out
     * of the {@code messages.properties} resource bundle sitting in the same
     * package. If the key does not exist — maybe a typo or a stale reference —
     * we return {@code !key!} so it stands out visibly in the UI rather than
     * silently swallowing the problem.
     *
     * <p>For example — C-3PO searches his databanks for phrase code "GREET_HAN":</p>
     * <pre>
     *   String greeting = Messages.getString("GREET_HAN");
     *   // Returns "Hello, I am C-3PO, human-cyborg relations." if found
     *   // Returns "!GREET_HAN!" if the key is missing from the .properties file
     * </pre>
     *
     * @param key  The resource bundle key — same string used in the .properties file.
     * @return     The localised string for that key, or {@code !key!} if the key is missing.
     */
    public static String getString( String key )
    {
        try
        {
            return RESOURCE_BUNDLE.getString( key );
        }
        catch ( MissingResourceException e )
        {
            return '!' + key + '!';
        }
    }
}
