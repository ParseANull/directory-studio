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
package org.apache.directory.studio.combinededitor.editor;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO Names the Tantive IV's Bridge Stations ───────────
// On the Tantive IV's bridge, C-3PO labels each station ("Navigation", "Comms",
// "Weapons") so every crew member knows what they're sitting at.
// This class does the same for the editor package: every tab label, dialog
// title, and error text in the editor sub-package is fetched here.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Centralises string lookups for the combinededitor editor package.
 * Tab names like "Template Editor", "Table Editor", and "LDIF Editor" come
 * from {@code combinededitor/editor/messages.properties} via this class.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    private static final String BUNDLE_NAME = "org.apache.directory.studio.combinededitor.editor.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );


    // ── C-3PO Is Consulted, Not Instantiated ──────────────────────────────────
    // You ask C-3PO for a translation; you don't keep one in every room.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Prevents instantiation — all lookups are static.
     */
    private Messages()
    {
    }


    // ── C-3PO Reads the Station Label from His Databanks ─────────────────────
    // C-3PO consults his extensive cross-reference tables and returns the label
    // in the correct language for this station; if the code doesn't exist he
    // signals it loudly with exclamation marks.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised string for the given key, or {@code !key!} if missing.
     *
     * @param key  the bundle key, e.g. {@code "LdifEditorPage.LDIFEditor"}.
     * @return     the localised string, or {@code !key!} if not found.
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
