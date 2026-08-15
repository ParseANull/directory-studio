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
package org.apache.directory.studio.templateeditor.editor.widgets;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO ON DUTY AT THE TANTIVE IV CONTROL PANELS ─────────────
// C-3PO is stationed at the control panel corridor, ready to translate every label
// and tooltip text that appears on the widget buttons and status readouts. Hand him
// a key, get back the human-readable string from the widgets bundle.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Static utility for looking up localized strings from the editor widgets package
 * message bundle ({@code editor/widgets/messages.properties}). All user-visible
 * text in the template editor widgets (button tooltips, error dialogs, placeholder
 * text) comes from here. Returns {@code !key!} for missing keys so broken strings
 * are immediately visible.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    private static final String BUNDLE_NAME = "org.apache.directory.studio.templateeditor.editor.widgets.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );


    // ── PRIVATE CONSTRUCTOR: ONE C-3PO PER PANEL SECTION ─────────────────────────
    // Static-only utility class — never instantiated.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Private — use the static {@link #getString(String)} method.
     */
    private Messages()
    {
    }


    // ── GET STRING: 3PO DELIVERS THE PANEL LABEL ─────────────────────────────────
    // C-3PO reads the panel label from the bundle and hands it over. Returns
    // {@code !key!} if the bundle doesn't have that label so broken strings stand out.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localized string for the given key from the widgets bundle.
     * Returns {@code !key!} if the key is not found.
     *
     * @param key  the message key; never {@code null}
     * @return the localized string, or {@code !key!} if missing
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
