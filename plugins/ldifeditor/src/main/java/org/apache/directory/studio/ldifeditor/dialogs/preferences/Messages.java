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

package org.apache.directory.studio.ldifeditor.dialogs.preferences;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — REBEL SIGNAL TRANSLATION UNIT (PREFERENCES) ─────────────
// Same pattern as the dialogs translation unit, just scoped to the preferences
// sub-package.  Every preference page label and tooltip comes through here.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Utility class for looking up localised strings for the
 * {@code dialogs.preferences} package.
 * Delegates to the {@link ResourceBundle} at
 * {@code org.apache.directory.studio.ldifeditor.dialogs.preferences.messages}.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── LOOK UP A LOCALISED STRING ────────────────────────────────────────────
    /**
     * Returns the localised string for {@code key} from the package resource bundle.
     * Returns {@code !key!} if the key is missing.
     *
     * @param key  the message key
     * @return     the localised string, or {@code !key!} if not found
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
