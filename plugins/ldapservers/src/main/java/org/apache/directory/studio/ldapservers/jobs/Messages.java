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

package org.apache.directory.studio.ldapservers.jobs;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO'S JOBS TRANSLATION MODULE ────────────────────────────────────────
// C-3PO has a translation module specialized for mission briefings and status updates —
// when the Death Star's crew needs a progress report worded for human ears, C-3PO looks
// up the right phrasing from his briefings module.
// This is the NLS helper for the {@code jobs} sub-package: progress task names, error
// messages for background jobs, and other user-visible strings live in messages.properties.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Loads and exposes localized strings for the {@code ldapservers.jobs} package.
 * Background jobs (start, stop, delete, open-config) use this class to fetch their
 * progress-monitor task names and error messages in the current locale.
 * Think of it as C-3PO's mission-briefing translation module — every job status update
 * spoken in plain language.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── C-3PO Translates The Mission Briefing ───────────────────────────────────────────────
    // An officer hands C-3PO a status code and C-3PO returns the correct mission-briefing phrase.
    // Unknown codes get a bracketed placeholder so gaps in the translation are impossible to miss.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localized string for the given key from this package's {@code messages.properties}.
     * Returns {@code !key!} for any missing key so translation gaps are immediately visible.
     *
     * @param key  the message key matching an entry in messages.properties
     * @return the localized string, or {@code !key!} if the key is absent
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
