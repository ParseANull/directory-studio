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
package org.apache.directory.studio.connection.ui.actions;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO'S TRANSLATION UNIT (actions sub-package) ─────────────
// Same pattern as the root Messages class, but scoped to the actions sub-package.
// Loads the messages.properties file from this package.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * NLS helper for the {@code connection.ui.actions} package.
 * Loads {@code messages.properties} from the same package directory.
 *
 * <p>Usage: {@code Messages.getString("SomeKey")}.
 * If the key is not found, returns {@code "!SomeKey!"} to make missing
 * translations obvious during development.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class Messages
{
    /** The resource bundle loaded from {@code <package>/messages.properties}. */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    /**
     * Prevents instantiation.  All methods are static.
     */
    private Messages()
    {
    }


    // ── GET STRING — LOOK UP A LOCALISED STRING ───────────────────────────────────
    /**
     * Returns the localised string for the given key.
     * Returns {@code "!key!"} if the key is missing.
     *
     * @param key  The message key defined in {@code messages.properties}.
     * @return  The localised string, or {@code "!key!"} if not found.
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
