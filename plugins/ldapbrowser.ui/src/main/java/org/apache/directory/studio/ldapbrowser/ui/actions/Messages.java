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

package org.apache.directory.studio.ldapbrowser.ui.actions;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO TRANSLATING FROM THE PROTOCOL ARCHIVES ───────────
// C-3PO is fluent in over six million forms of communication — he reads raw
// key codes from the Empire's protocol archives and returns the human-readable
// phrase that goes with them. This class does the same: it's an NLS (National
// Language Support) bundle accessor that maps string keys like
// "CopyDnAction.CopyDN" to localised display strings from the messages.properties
// file. If a key is missing, we return "!key!" so it's obvious something went wrong.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * NLS message bundle accessor for the {@code ldapbrowser.ui.actions} package.
 * All user-visible strings in this package are externalised into a
 * {@code messages.properties} file and accessed through this class so that
 * localisation (adding new language files) requires no code changes.
 * Think of this as C-3PO reading the protocol archives — give him a key,
 * he hands back the right phrase in the right language.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    /**
     * Get back a message from the resource file given a key
     *
     * @param key The key associated with the message
     * @return The found message
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
