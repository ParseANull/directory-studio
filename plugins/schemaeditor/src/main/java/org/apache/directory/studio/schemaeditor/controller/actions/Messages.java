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
package org.apache.directory.studio.schemaeditor.controller.actions;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO TRANSLATING R2-D2'S BEEPS ────────────────────────
// Aboard the Tantive IV, R2-D2 lets out a rapid burst of electronic whistles
// and beeps — completely unintelligible to the humans around him.  C-3PO steps
// in, listens to the sequence, and translates it into plain English: "He says
// the possibility of successfully navigating an asteroid field is approximately
// 3,720 to one."  The key → human-readable string mapping is exactly this.
// This class is our C-3PO: callers hand us an opaque message key (R2's beep)
// and we hand back the localized, human-readable string from the bundle.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Utility class for looking up localized strings in the {@code messages}
 * resource bundle for the {@code controller.actions} package.
 * Every action class that needs a user-visible label, tooltip, or error message
 * calls {@link #getString(String)} with the appropriate key rather than
 * hard-coding English text.
 * Think of this class as C-3PO: he knows every language in the galaxy so the
 * rest of the crew doesn't have to.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO LISTENS TO R2'S BEEP AND TRANSLATES ────────────────────────────
    // R2 emits a short trill — C-3PO cocks his golden head, parses the sequence,
    // and delivers the English equivalent to the nearest human.
    // If R2 says something C-3PO has never heard before, he announces "I don't
    // know what that means" — we wrap the unknown key in exclamation marks so
    // it is visibly wrong rather than silently absent.
    // "I am fluent in over six million forms of communication!" — but not that one.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a localized string by key from the package-level messages bundle.
     * If the key doesn't exist in the bundle — e.g. a typo or a missing entry —
     * we return {@code !key!} rather than throwing, so the UI renders something
     * obviously wrong instead of crashing.
     *
     * @param key  the message key to look up; must match an entry in messages.properties
     * @return     the localized string for that key, or {@code !key!} if not found
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
