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
package org.apache.directory.studio.schemaeditor.view.widget;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — LEIA'S HOLOGRAM MESSAGE TO OBI-WAN ─────────────────────
// "Help me, Obi-Wan Kenobi. You're my only hope." Leia recorded her plea into R2,
// and when the time came, R2 played it back word for word. No interpretation, no
// improvisation — just faithful delivery of the pre-recorded message.
// Our Messages class does the same: it holds a reference to a resource bundle and,
// when asked for a key, plays back the corresponding localised string — or wraps
// the key in exclamation marks if it was never recorded in the first place.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A thin static utility that looks up UI strings from the {@code messages.properties}
 * resource bundle for the {@code org.apache.directory.studio.schemaeditor.view.widget}
 * package. Every user-visible string in this package goes through here so we keep
 * all translations in one place and out of the source code.
 * Think of it as R2-D2 playing back Leia's hologram: the message is stored once in
 * the resource bundle, and we deliver it faithfully on demand.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── R2 PLAYS BACK THE HOLOGRAM FOR THE GIVEN KEY ─────────────────────────────
    // Obi-Wan asks R2 to replay Leia's message. R2 locates it in the recording and
    // projects it. If R2 somehow has no recording for that sequence, he projects an
    // error indicator — in our case, "!key!" — so the missing string is obvious in
    // the UI rather than silently swallowed.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a localised string from the resource bundle by key. If the key is not
     * found (perhaps because someone forgot to add it, or a typo crept in), we return
     * {@code "!key!"} instead of throwing — making the missing string visible in the UI
     * rather than crashing.
     *
     * <p>For example — R2 plays back Leia's message:</p>
     * <pre>
     *   getString("CoreSchemasSelectionWidget.ApacheDS")  →  "ApacheDS"
     *   getString("typo.key.nobody.added")               →  "!typo.key.nobody.added!"
     * </pre>
     *
     * @param key  the message key from the {@code messages.properties} file
     * @return     the localised string for that key, or {@code "!key!"} if not found
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
