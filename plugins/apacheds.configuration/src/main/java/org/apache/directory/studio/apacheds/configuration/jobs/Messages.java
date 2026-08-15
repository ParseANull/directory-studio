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
package org.apache.directory.studio.apacheds.configuration.jobs;


import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;


// ── CLASS: Messages — C-3PO'S CONFIGURATION JOBS TRANSLATION MODULE ──────────────────────
// The jobs package handles background operations like loading and saving server configuration.
// Progress messages, error strings, and operation labels for those background tasks all
// live in this C-3PO sub-dictionary for the jobs package.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * Loads and exposes localized strings for the {@code apacheds.configuration.jobs} package.
 * Contains progress messages and error text used by background configuration load/save jobs.
 * Think of it as C-3PO's jobs package sub-dictionary.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages extends NLS
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── C-3PO Looks Up A Job Progress String ──────────────────────────────────────────────────
    // A background job asks C-3PO for its progress text or error message.
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
