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
package org.apache.directory.studio.aciitemeditor.widgets;


import java.util.MissingResourceException;
import java.util.ResourceBundle;

// ── CLASS: Messages — THE DEATH STAR INTERCOM DIRECTORY ─────────────────────
// On the Death Star, every department posts its communiqués to a central
// intercom board; officers look up a code word and the full message drops out.
// That is exactly what this class does for the widgets package.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * NLS message lookup utility for the {@code widgets} package.
 * Wraps a {@link ResourceBundle} so that every widget composite can retrieve
 * its localised strings by key without handling {@link MissingResourceException}
 * themselves.
 * Think of this class as the Death Star intercom directory: every station
 * has a code word, and this class finds the matching announcement.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── LOOK UP THE ANNOUNCEMENT ──────────────────────────────────────────────
    // An Imperial officer punches a code into the intercom panel and receives
    // the pre-recorded message for that station.
    // If the code is unknown, the panel blinks "!code!" so the fault is obvious.
    // We do the same: return the localised string or a bracketed sentinel.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised string for {@code key}.
     * If no entry exists in the resource bundle, we return {@code '!' + key + '!'}
     * as a visible sentinel so missing keys are easy to spot during development.
     *
     * <p>For example — the intercom panel receives code {@code "ACIItemGeneralComposite.label.tag"}:</p>
     * <pre>
     *   String label = Messages.getString("ACIItemGeneralComposite.label.tag");
     *   // Returns e.g. "Identification Tag" from messages.properties
     * </pre>
     *
     * @param key  the resource-bundle key for the desired string
     * @return     the localised message, or {@code "!key!"} when the key is absent
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
