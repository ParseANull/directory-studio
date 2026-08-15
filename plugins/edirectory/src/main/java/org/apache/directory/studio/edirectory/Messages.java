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
package org.apache.directory.studio.edirectory;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO Translating Novell eDirectory Status Labels ───────
// The eDirectory plugin needs human-readable strings for its UI labels and
// error messages.  C-3PO translates them from the phrase book (messages.properties)
// on demand.  If a phrase isn't found, he wraps the key in exclamation marks
// so the developer sees the gap immediately.
// Messages is C-3PO for this plugin: a static ResourceBundle wrapper.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Looks up localised strings from this plugin's {@code messages.properties} file.
 * Returns {@code "!" + key + "!"} for any key that is not found.
 * Think of this as C-3PO translating eDirectory panel labels into plain Basic.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── C-3PO Has No Constructor — He Just Looks Things Up ────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor — static utility class; no instantiation needed.
     */
    private Messages()
    {
    }


    // ── Translate the Key into a UI String ────────────────────────────────────
    // Looks up the key in the bundle; returns "!key!" if not found.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised string for the given message key.
     * Returns {@code "!" + key + "!"} if the key is not found.
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
