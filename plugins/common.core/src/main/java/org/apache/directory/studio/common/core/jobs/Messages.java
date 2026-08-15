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

package org.apache.directory.studio.common.core.jobs;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO Relaying Yavin 4 Mission Status Labels ────────────
// C-3PO knows 6 million forms of communication; we just need him to look up
// a human-readable label from the Alliance's mission-status phrase book (the
// properties file) given a key.  If the phrase book doesn't have an entry for
// the key, C-3PO falls back to wrapping the key in exclamation marks so the
// developer can spot the missing translation immediately.
// Messages is the Yavin 4 phrase book: a thin static wrapper around a
// ResourceBundle that turns message keys into UI strings.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Looks up localised strings from the common-core {@code messages.properties} file.
 * Call {@link #getString(String)} with a message key to get the corresponding
 * human-readable string.  If the key is missing, the returned string is
 * {@code "!" + key + "!"} so the gap is immediately visible.
 * Think of this as C-3PO translating Alliance mission-status codes into plain
 * Basic for the operators in Yavin 4 mission control.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── Prevent Instantiation — C-3PO Doesn't Have an Off Button ─────────────
    // This class is all static; there is no reason to create an instance.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor — this is a static utility class; instantiation is not
     * needed or permitted.
     */
    private Messages()
    {
    }


    // ── Look Up a Phrase in the Alliance Phrase Book ──────────────────────────
    // C-3PO consults the phrase book; if the key is there, he reads the label
    // aloud.  If not, he wraps the key in exclamation marks to flag the gap.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised string for the given message key.
     * If the key has no entry in the properties file, we return
     * {@code "!" + key + "!"} so the missing translation is easy to spot.
     *
     * <p>For example — C-3PO checks the phrase book:</p>
     * <pre>
     *   getString("StudioProgressMonitor.CheckCancellation")
     *   // returns "Checking for cancellation..." (from messages.properties)
     *   getString("Unknown.Key")
     *   // returns "!Unknown.Key!" (key not found)
     * </pre>
     *
     * @param key  the message key to look up.
     * @return     the localised string, or {@code "!" + key + "!"} if not found.
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
