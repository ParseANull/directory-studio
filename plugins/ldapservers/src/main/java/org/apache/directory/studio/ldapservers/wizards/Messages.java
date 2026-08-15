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
package org.apache.directory.studio.ldapservers.wizards;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO'S NEW SERVER WIZARD TRANSLATION MODULE ────────────────────────
// The New Server Wizard speaks its own language: "Choose the type of server", "Enter a name",
// "Creating LDAP Server...".  C-3PO keeps a sub-dictionary specifically for the wizard's text.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Loads and exposes localized strings for the {@code ldapservers.wizards} package.
 * These include wizard page titles, descriptions, validation messages, and progress texts.
 * Think of it as C-3PO's New Server Wizard sub-dictionary.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── C-3PO Looks Up A Wizard String ───────────────────────────────────────────────────────
    // A wizard page asks C-3PO for its title or validation message.
    // Unknown keys become bracketed placeholders — clearly wrong, immediately visible.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localized string for the given key from this package's {@code messages.properties}.
     * Returns {@code !key!} for missing keys.
     *
     * @param key  the message key
     * @return the localized string, or {@code !key!} if absent
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
