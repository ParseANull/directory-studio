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
package org.apache.directory.studio.ldifeditor.dialogs;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — REBEL SIGNAL TRANSLATION UNIT (DIALOGS) ────────────────
// Every Alliance unit that broadcasts a message uses the same signal codebook
// so there is no confusion when the words hit the display.
// This utility class looks up localised dialog strings from the package
// resource bundle and returns the key wrapped in "!" markers if the key is
// missing, making translation gaps obvious during development.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Utility class for looking up localised strings for the {@code dialogs} package.
 * Delegates to the {@link ResourceBundle} found at
 * {@code org.apache.directory.studio.ldifeditor.dialogs.messages}.
 * Think of this as the Alliance signal translation unit: turn a message key
 * into the right words for the current locale.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── LOOK UP A LOCALISED STRING ────────────────────────────────────────────
    // The comms officer checks the codebook for the key and reads out the
    // translation; if the key is missing they broadcast "!key!" so everyone
    // knows a translation is absent.
    /**
     * Returns the localised string for {@code key} from the package resource bundle.
     * If the key is not found returns {@code !key!} so missing translations are
     * immediately visible.
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
