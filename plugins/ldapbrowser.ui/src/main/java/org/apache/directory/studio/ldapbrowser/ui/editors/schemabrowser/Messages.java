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

package org.apache.directory.studio.ldapbrowser.ui.editors.schemabrowser;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO Reading The Jawa Dialect ─────────────────────────
// In the Jundland Wastes, C-3PO listens to the Jawas' rapid, beeping chatter
// and converts it into plain Basic that Luke and Uncle Owen can actually use.
// This class does exactly that: it takes opaque string keys from the code and
// returns the human-readable label stored in the .properties bundle.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Looks up localized UI strings from the schemabrowser messages resource bundle.
 * We use this everywhere we need a label, tooltip, or error message so the
 * actual text lives in one place and can be translated without touching Java code.
 * Think of this class as C-3PO: it fluently translates machine keys into words
 * a human can read, returning "!key!" if no translation exists so failures are
 * obvious rather than silent.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO Translates A Jawa Phrase ────────────────────────────────────────────
    // C-3PO hears a rapid string of Jawa dialect and looks it up in his six-million
    // language database, returning the Basic equivalent so the crew understands.
    // If the phrase is in no database, he announces "unknown dialect" rather than
    // going silent — here we return "!key!" so the missing entry is unmistakable.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up the human-readable string for the given message key.
     * We wrap the ResourceBundle lookup so callers never have to catch
     * MissingResourceException; instead they get a visible "!key!" placeholder
     * that makes missing translations easy to spot during testing.
     *
     * <p>For example — C-3PO translates a Jawa utterance:</p>
     * <pre>
     *   key   = "ReloadSchemaAction.ReloadSchema"
     *   result = "Reload Schema"   // from messages.properties
     *   // or "!ReloadSchemaAction.ReloadSchema!" if the key is absent
     * </pre>
     *
     * @param key  the message key defined in messages.properties; must not be null
     * @return     the translated string, or "!key!" if the key is not found
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
